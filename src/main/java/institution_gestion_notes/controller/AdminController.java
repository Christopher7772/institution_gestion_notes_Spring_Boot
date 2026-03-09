package institution_gestion_notes.controller;

import institution_gestion_notes.entity.*;
import institution_gestion_notes.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AdminController
 * Structure et logique conservées selon tes instructions.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final EtudiantRepository etudiantRepository;
    private final ProfesseurRepository professeurRepository;
    private final MatiereRepository matiereRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository,
                           EtudiantRepository etudiantRepository,
                           ProfesseurRepository professeurRepository,
                           MatiereRepository matiereRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.etudiantRepository = etudiantRepository;
        this.professeurRepository = professeurRepository;
        this.matiereRepository = matiereRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------------- USERS ----------------------
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        User saved = userRepository.save(user);
        saved.setPassword(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ---------------------- ETUDIANTS ----------------------
    @PostMapping("/etudiants")
    public ResponseEntity<Etudiant> createEtudiant(@RequestBody Etudiant etudiant) {
        Etudiant saved = etudiantRepository.save(etudiant);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/etudiants")
    public ResponseEntity<List<Etudiant>> listEtudiants() {
        return ResponseEntity.ok(etudiantRepository.findAll());
    }

    @GetMapping("/etudiants/{id}")
    public ResponseEntity<Etudiant> getEtudiantById(@PathVariable Long id) {
        Etudiant e = etudiantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Étudiant introuvable"));
        return ResponseEntity.ok(e);
    }

    @PutMapping("/etudiants/{id}")
    public ResponseEntity<Etudiant> updateEtudiant(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Etudiant e = etudiantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Étudiant introuvable"));

        if (payload.containsKey("nom")) e.setNom(payload.get("nom") == null ? null : payload.get("nom").toString());
        if (payload.containsKey("prenom")) e.setPrenom(payload.get("prenom") == null ? null : payload.get("prenom").toString());
        if (payload.containsKey("codeEtudiant")) e.setCodeEtudiant(payload.get("codeEtudiant") == null ? null : payload.get("codeEtudiant").toString());
        if (payload.containsKey("numeroEtudiant")) e.setNumeroEtudiant(payload.get("numeroEtudiant") == null ? null : payload.get("numeroEtudiant").toString());
        if (payload.containsKey("classe")) e.setClasse(payload.get("classe") == null ? null : payload.get("classe").toString());

        try {
            Etudiant saved = etudiantRepository.save(e);
            return ResponseEntity.ok(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Contrainte BD violée : " + ex.getRootCause());
        }
    }

    @DeleteMapping("/etudiants/{id}")
    public ResponseEntity<Void> deleteEtudiant(@PathVariable Long id) {
        Etudiant e = etudiantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Étudiant introuvable"));
        try {
            etudiantRepository.delete(e);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible de supprimer : dépendances existantes");
        }
    }

    // ---------------------- PROFESSEURS ----------------------
    @PostMapping("/professeurs")
    public ResponseEntity<Professeur> createProfesseur(@RequestBody Professeur professeur) {
        // Vérif email unique (fallback safe)
        if (professeur.getEmail() != null) {
            boolean existsByEmail = professeurRepository.findAll().stream()
                    .anyMatch(p -> p.getEmail() != null && p.getEmail().equalsIgnoreCase(professeur.getEmail()));
            if (existsByEmail) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cet email est déjà utilisé.");
        }

        Professeur saved = professeurRepository.save(professeur);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/professeurs")
    public ResponseEntity<List<Professeur>> listProfesseurs() {
        return ResponseEntity.ok(professeurRepository.findAll());
    }

    // AJOUT : Get professeur by ID
    @GetMapping("/professeurs/{id}")
    public ResponseEntity<Professeur> getProfesseurById(@PathVariable Long id) {
        Professeur p = professeurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professeur introuvable"));
        return ResponseEntity.ok(p);
    }

    // AJOUT : Update Professeur (Même logique que updateMatiere)
    @PutMapping("/professeurs/{id}")
    public ResponseEntity<Professeur> updateProfesseur(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Professeur existing = professeurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professeur introuvable"));

        if (payload.containsKey("nom")) {
            existing.setNom(payload.get("nom") == null ? null : payload.get("nom").toString().trim());
        }
        if (payload.containsKey("prenom")) {
            existing.setPrenom(payload.get("prenom") == null ? null : payload.get("prenom").toString().trim());
        }
        if (payload.containsKey("specialite")) {
            existing.setSpecialite(payload.get("specialite") == null ? null : payload.get("specialite").toString().trim());
        }
        
        // Email (vérif doublon)
        if (payload.containsKey("email")) {
            String mail = payload.get("email") == null ? null : payload.get("email").toString().trim();
            if (mail != null && !mail.isEmpty()) {
                boolean clash = professeurRepository.findAll().stream()
                        .anyMatch(x -> x.getEmail() != null && x.getEmail().equalsIgnoreCase(mail) && !Objects.equals(x.getIdProfesseur(), id));
                if (clash) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cet email appartient déjà à un autre professeur.");
                existing.setEmail(mail);
            }
        }

        // Numero Professeur (vérif doublon)
        if (payload.containsKey("numeroProfesseur")) {
            String num = payload.get("numeroProfesseur") == null ? null : payload.get("numeroProfesseur").toString().trim();
            if (num != null && !num.isEmpty()) {
                boolean clash = professeurRepository.findAll().stream()
                        .anyMatch(x -> x.getNumeroProfesseur() != null && x.getNumeroProfesseur().equalsIgnoreCase(num) && !Objects.equals(x.getIdProfesseur(), id));
                if (clash) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce numéro de professeur existe déjà.");
                existing.setNumeroProfesseur(num);
            }
        }

        try {
            Professeur saved = professeurRepository.save(existing);
            return ResponseEntity.ok(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Contrainte BD violée : " + ex.getRootCause());
        }
    }

    // AJOUT : Delete Professeur
    @DeleteMapping("/professeurs/{id}")
    public ResponseEntity<Void> deleteProfesseur(@PathVariable Long id) {
        Professeur p = professeurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professeur introuvable"));
        try {
            professeurRepository.delete(p);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible de supprimer : le professeur est lié à des matières.");
        }
    }

    // ---------------------- MATIERES ----------------------
    @PostMapping("/matieres")
    public ResponseEntity<Matiere> createMatiere(@RequestBody Matiere matiere) {
        if (matiere.getNom() != null) {
            boolean existsByName = matiereRepository.findAll().stream()
                    .anyMatch(m -> m.getNom() != null && m.getNom().equalsIgnoreCase(matiere.getNom()));
            if (existsByName) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une matière avec ce nom existe déjà.");
        }
        if (matiere.getCode() != null) {
            boolean existsByCode = matiereRepository.findAll().stream()
                    .anyMatch(m -> m.getCode() != null && m.getCode().equalsIgnoreCase(matiere.getCode()));
            if (existsByCode) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une matière avec ce code existe déjà.");
        }
        if (matiere.getProfesseur() != null && matiere.getProfesseur().getIdProfesseur() != null) {
            Long profId = matiere.getProfesseur().getIdProfesseur();
            Professeur prof = professeurRepository.findById(profId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Professeur introuvable"));
            matiere.setProfesseur(prof);
        }
        try {
            Matiere saved = matiereRepository.save(matiere);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Contrainte BD violée : " + ex.getRootCause());
        }
    }

    @GetMapping("/matieres")
    public ResponseEntity<List<Matiere>> listMatieres() {
        return ResponseEntity.ok(matiereRepository.findAll());
    }

    @GetMapping("/matieres/{id}")
    public ResponseEntity<Matiere> getMatiereById(@PathVariable Long id) {
        Matiere m = matiereRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matière introuvable"));
        return ResponseEntity.ok(m);
    }

    @PutMapping("/matieres/{id}")
    public ResponseEntity<Matiere> updateMatiere(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Matiere existing = matiereRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matière introuvable"));

        if (payload.containsKey("nom")) {
            String nouveauNom = payload.get("nom") == null ? null : payload.get("nom").toString().trim();
            if (nouveauNom != null && !nouveauNom.isEmpty()) {
                boolean clash = matiereRepository.findAll().stream()
                        .anyMatch(x -> x.getNom() != null && x.getNom().equalsIgnoreCase(nouveauNom) && !Objects.equals(x.getIdMatiere(), id));
                if (clash) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une matière avec ce nom existe déjà.");
                existing.setNom(nouveauNom);
            }
        }

        if (payload.containsKey("code")) {
            Object codeObj = payload.get("code");
            String code = codeObj == null ? null : codeObj.toString().trim();
            if (code != null && !code.isEmpty()) {
                boolean clash = matiereRepository.findAll().stream()
                        .anyMatch(x -> x.getCode() != null && x.getCode().equalsIgnoreCase(code) && !Objects.equals(x.getIdMatiere(), id));
                if (clash) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une matière avec ce code existe déjà.");
                existing.setCode(code);
            } else {
                existing.setCode(null);
            }
        }

        if (payload.containsKey("coefficient")) {
            try {
                Object c = payload.get("coefficient");
                Integer coef = (c instanceof Number) ? ((Number) c).intValue() : Integer.valueOf(c.toString());
                existing.setCoefficient(coef);
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coefficient invalide");
            }
        }

        if (payload.containsKey("responsable")) {
            Object resp = payload.get("responsable");
            if (resp instanceof Map) {
                Object rawId = ((Map<?, ?>) resp).get("idProfesseur");
                if (rawId != null) {
                    try {
                        Long profId = (rawId instanceof Number) ? ((Number) rawId).longValue() : Long.valueOf(rawId.toString());
                        Professeur prof = professeurRepository.findById(profId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Professeur introuvable"));
                        existing.setProfesseur(prof);
                    } catch (NumberFormatException nfe) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID Professeur invalide");
                    }
                } else {
                    existing.setProfesseur(null);
                }
            }
        }

        try {
            Matiere saved = matiereRepository.save(existing);
            return ResponseEntity.ok(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Contrainte BD violée : " + ex.getRootCause());
        }
    }

    @DeleteMapping("/matieres/{id}")
    public ResponseEntity<Void> deleteMatiere(@PathVariable Long id) {
        Matiere m = matiereRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matière introuvable"));
        try {
            matiereRepository.delete(m);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible de supprimer : dépendances existantes");
        }
    }
}