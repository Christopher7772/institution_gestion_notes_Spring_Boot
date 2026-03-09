package institution_gestion_notes.controller;

import institution_gestion_notes.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/etudiant")
public class EtudiantController {

    private final NoteService noteService;

    public EtudiantController(NoteService noteService) {
        this.noteService = noteService;
    }

    // Récupérer la moyenne (bulletin simplifié)
    @GetMapping("/{id}/bulletin")
    public ResponseEntity<Double> getBulletin(@PathVariable Long id, @RequestParam int semestre) {
        double moyenne = noteService.calculerMoyenneParEtudiantEtSemestre(id, semestre);
        return ResponseEntity.ok(moyenne);
    }
}
