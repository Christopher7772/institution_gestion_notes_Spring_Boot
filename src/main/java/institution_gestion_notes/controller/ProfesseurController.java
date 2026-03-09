package institution_gestion_notes.controller;

import institution_gestion_notes.entity.Etudiant;
import institution_gestion_notes.entity.Note;
import institution_gestion_notes.entity.Professeur;
import institution_gestion_notes.repository.EtudiantRepository;
import institution_gestion_notes.repository.NoteRepository;
import institution_gestion_notes.repository.ProfesseurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professeur")
public class ProfesseurController {

    private final NoteRepository noteRepository;
    private final ProfesseurRepository professeurRepository;
    private final EtudiantRepository etudiantRepository;

    // Mise à jour du constructeur pour inclure les nouveaux repositories
    public ProfesseurController(NoteRepository noteRepository, 
                                ProfesseurRepository professeurRepository,
                                EtudiantRepository etudiantRepository) {
        this.noteRepository = noteRepository;
        this.professeurRepository = professeurRepository;
        this.etudiantRepository = etudiantRepository;
    }

    // AJOUT PRO : Récupérer les étudiants par matière
    @GetMapping("/etudiants-par-matiere/{idMatiere}")
    public ResponseEntity<List<Etudiant>> getEtudiantsByMatiere(@PathVariable Long idMatiere, Authentication auth) {
        // Optionnel : On pourrait vérifier ici si la matière appartient bien au prof connecté
        List<Etudiant> etudiants = etudiantRepository.findByMatieresIdMatiere(idMatiere);
        return ResponseEntity.ok(etudiants);
    }

    // AJOUT PRO : Sauvegarder un lot de notes (Batch)
    // AJOUT PRO : Sauvegarder un lot de notes (Batch) avec logique "Upsert"
@PostMapping("/notes/batch")
public ResponseEntity<?> saveAllNotes(@RequestBody List<Note> notes, Authentication auth) {
    Professeur prof = professeurRepository.findByUserEmail(auth.getName()).orElse(null);
    if (prof == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    for (Note note : notes) {
        // Logique "Update if exists, Insert sinon" :
        // On cherche si une note existe déjà pour ce trio unique : Étudiant + Matière + Semestre
        noteRepository.findByEtudiantIdEtudiantAndMatiereIdMatiereAndSemestre(
            note.getEtudiant().getIdEtudiant(),
            note.getMatiere().getIdMatiere(),
            note.getSemestre()
        ).ifPresent(existingNote -> {
            // Si elle existe, on récupère son ID original. 
            // JPA comprendra alors qu'il doit faire un UPDATE et non un INSERT.
            note.setIdNote(existingNote.getIdNote());
        });

        // On s'assure que toutes les notes sont attribuées au professeur connecté
        note.setProfesseur(prof);
    }
    
    // Sauvegarde groupée (Update pour les IDs connus, Insert pour les nouveaux)
    noteRepository.saveAll(notes);
    return ResponseEntity.ok().body("{\"message\": \"Notes enregistrées avec succès\"}");
}

    // Ton code existant conservé
    @PostMapping("/notes")
    public ResponseEntity<Note> addNote(@RequestBody Note note) {
        if (note.getValeur() < 0 || note.getValeur() > 100) {
            return ResponseEntity.badRequest().build();
        }
        Note saved = noteRepository.save(note);
        return ResponseEntity.status(201).body(saved);
    }
    
    @DeleteMapping("/notes/{id}")
public ResponseEntity<?> deleteNote(@PathVariable Long id, Authentication auth) {
    return noteRepository.findById(id).map(note -> {
        // Optionnel : Vérifier que la note appartient bien au prof connecté
        // if (!note.getProfesseur().getUser().getEmail().equals(auth.getName())) {
        //    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        // }
        
        noteRepository.delete(note);
        return ResponseEntity.ok().body("{\"message\": \"Note supprimée avec succès\"}");
    }).orElse(ResponseEntity.notFound().build());
}
}