package institution_gestion_notes.controller;

import institution_gestion_notes.entity.Etudiant;
import institution_gestion_notes.entity.Matiere;
import institution_gestion_notes.entity.Note;
import institution_gestion_notes.repository.EtudiantRepository;
import institution_gestion_notes.repository.MatiereRepository;
import institution_gestion_notes.repository.NoteRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin REST controller (final version) :
 * - Pas de DTO (retour Map<String,Object>)
 * - Extraction safe des valeurs de Note (méthode extractNoteValueSafe)
 * - Validation des ids de matières
 * - @Transactional sur méthodes qui parcourent relations LAZY
 */
@RestController
@RequestMapping("/api/admin/etudiants")
public class AdminRestController {

    private final EtudiantRepository etudiantRepo;
    private final MatiereRepository matRepo;
    private final NoteRepository noteRepo;

    public AdminRestController(EtudiantRepository etudiantRepo,
                               MatiereRepository matRepo,
                               NoteRepository noteRepo) {
        this.etudiantRepo = etudiantRepo;
        this.matRepo = matRepo;
        this.noteRepo = noteRepo;
    }

    // ------------------------
    // 1) Matières + assigned flag
    // ------------------------
    @GetMapping("/{id}/matieres")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMatieresEtudiant(@PathVariable Long id) {
        Etudiant etudiant = etudiantRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Étudiant introuvable"));

        Set<Long> assignedIds = etudiant.getMatieres() == null
                ? Collections.emptySet()
                : etudiant.getMatieres().stream()
                    .map(Matiere::getIdMatiere)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

        return matRepo.findAll().stream()
                .map(m -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("idMatiere", m.getIdMatiere());
                    map.put("nom", m.getNom());
                    map.put("code", m.getCode());
                    map.put("coefficient", m.getCoefficient());
                    map.put("assigned", assignedIds.contains(m.getIdMatiere()));
                    return map;
                })
                .collect(Collectors.toList());
    }

    // ------------------------
    // 2) Notes (liste) + moyenne simple (non pondérée)
    // ------------------------
    @GetMapping("/{id}/notes")
    @Transactional(readOnly = true)
    public Map<String, Object> getNotesEtudiant(@PathVariable Long id) {
        List<Note> notes = noteRepo.findByEtudiant_IdEtudiant(id);

        List<Map<String, Object>> notesJson = notes.stream()
                .map(n -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    double safeVal = extractNoteValueSafe(n);
                    m.put("matiere", n.getMatiere() != null ? n.getMatiere().getNom() : "N/A");
                    m.put("valeur", safeVal);       // valeur normalisée (double)
                    m.put("semestre", n.getSemestre());
                    return m;
                })
                .collect(Collectors.toList());

        // Moyenne non pondérée : ignorer valeurs "absentes" (ici safe extraction returns 0.0 for absent,
        // mais on souhaite ici calculer la moyenne seulement des notes réellement présentes).
        double moyenne = notes.stream()
                .filter(n -> hasNonNullNoteValue(n))
                .mapToDouble(AdminRestController::extractNoteValueSafe)
                .average()
                .orElse(Double.NaN);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("notes", notesJson);
        result.put("moyenne", Double.isNaN(moyenne) ? null : Math.round(moyenne * 100.0) / 100.0);
        return result;
    }

    // ------------------------
    // 3) Assigner matières (remplace la collection actuelle)
    // ------------------------
    @PostMapping("/{id}/assign-matieres")
    @Transactional
    public ResponseEntity<Map<String, Object>> assignMatieres(@PathVariable Long id,
                                                              @RequestBody List<Long> matieresIds) {
        Etudiant etudiant = etudiantRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Étudiant introuvable"));

        if (matieresIds == null || matieresIds.isEmpty()) {
            etudiant.setMatieres(new HashSet<>());
            etudiantRepo.save(etudiant);
            return ResponseEntity.ok(Map.of("message", "Toutes les matières retirées", "assignedCount", 0));
        }

        List<Matiere> matieres = matRepo.findAllById(matieresIds);

        // Validation : détecte les IDs manquants
        Set<Long> foundIds = matieres.stream()
                .map(Matiere::getIdMatiere)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Long> missing = matieresIds.stream()
                .filter(i -> !foundIds.contains(i))
                .collect(Collectors.toList());

        if (!missing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Certains idMatiere sont invalides : " + missing);
        }

        etudiant.setMatieres(new HashSet<>(matieres));
        etudiantRepo.save(etudiant);

        Set<Long> assigned = etudiant.getMatieres().stream()
                .map(Matiere::getIdMatiere)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(Map.of(
                "message", "Matières mises à jour",
                "assignedCount", assigned.size(),
                "assignedIds", assigned
        ));
    }

    // ------------------------
    // 4) Bulletin : moyenne pondérée par coefficient
    // ------------------------
    @GetMapping("/{id}/bulletin")
    @Transactional(readOnly = true)
    public Map<String, Object> getBulletin(@PathVariable Long id) {
        List<Note> notes = noteRepo.findByEtudiant_IdEtudiant(id);

        double weightedSum = 0.0;
        double coefSum = 0.0;
        List<Map<String,Object>> notesJson = new ArrayList<>();

        for (Note n : notes) {
            double val = extractNoteValueSafe(n); // safe (0.0 si absent)
            int coef = 1;
            if (n.getMatiere() != null && n.getMatiere().getCoefficient() != null) {
                coef = n.getMatiere().getCoefficient();
            }

            weightedSum += val * coef;
            coefSum += coef;

            notesJson.add(Map.of(
                    "matiere", n.getMatiere() != null ? n.getMatiere().getNom() : "N/A",
                    "valeur", val,
                    "semestre", n.getSemestre(),
                    "coefficient", coef
            ));
        }

        double moyennePonderee = coefSum > 0 ? (weightedSum / coefSum) : Double.NaN;

        return Map.of(
                "notes", notesJson,
                "moyennePonderee", Double.isNaN(moyennePonderee) ? null : Math.round(moyennePonderee * 100.0) / 100.0
        );
    }

    @PostMapping("/admin/matieres")
public String saveMatiere(Matiere matiere) {
    matRepo.save(matiere);
    // On force le navigateur à refaire un GET propre vers la liste
    return "redirect:/admin/matieres"; 
}
    // ------------------------
    // Helpers : safe extraction and presence check
    // ------------------------

    /**
     * Tente d'extraire la valeur numérique d'une Note, de façon robuste :
     * - Essaie getValeur() via reflection (primitives boxed automatiquement).
     * - Si absent, cherche champ 'valeur'.
     * - Supporte Number (Double, Integer, BigDecimal...) et parse String si nécessaire.
     * - Si rien de récupérable, retourne 0.0
     */
    private static double extractNoteValueSafe(Note note) {
        if (note == null) return 0.0;
        // 1) tenter getValeur()
        try {
            Method getter = note.getClass().getMethod("getValeur");
            Object valObj = getter.invoke(note);
            if (valObj == null) return 0.0;
            if (valObj instanceof Number) return ((Number) valObj).doubleValue();
            if (valObj instanceof String) {
                try { return Double.parseDouble(((String) valObj).trim()); } catch (NumberFormatException ignored) {}
            }
            if (valObj instanceof BigDecimal) return ((BigDecimal) valObj).doubleValue();
            // fallback toString parsing
            try { return Double.parseDouble(valObj.toString()); } catch (Exception ignored) {}
            return 0.0;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // 2) fallback : chercher champ 'valeur'
            try {
                Field field = note.getClass().getDeclaredField("valeur");
                field.setAccessible(true);
                Object valObj = field.get(note);
                if (valObj == null) return 0.0;
                if (valObj instanceof Number) return ((Number) valObj).doubleValue();
                if (valObj instanceof String) {
                    try { return Double.parseDouble(((String) valObj).trim()); } catch (NumberFormatException ignored) {}
                }
                if (valObj instanceof BigDecimal) return ((BigDecimal) valObj).doubleValue();
                try { return Double.parseDouble(valObj.toString()); } catch (Exception ignored) {}
                return 0.0;
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                return 0.0;
            }
        }
    }

    /**
     * Indique si la Note contient bien une valeur "non nulle / présente".
     * - True si getValeur() existe et renvoie un Number non-null
     * - Sinon false
     * Utilisé pour calculer la moyenne simple (ignorer entrées absentes).
     */
    private static boolean hasNonNullNoteValue(Note note) {
        if (note == null) return false;
        try {
            Method getter = note.getClass().getMethod("getValeur");
            Object valObj = getter.invoke(note);
            if (valObj == null) return false;
            if (valObj instanceof Number) return true;
            if (valObj instanceof String) {
                String s = ((String) valObj).trim();
                return !s.isEmpty();
            }
            return true;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            try {
                Field field = note.getClass().getDeclaredField("valeur");
                field.setAccessible(true);
                Object valObj = field.get(note);
                return valObj != null;
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                return false;
            }
        }
    }
}
