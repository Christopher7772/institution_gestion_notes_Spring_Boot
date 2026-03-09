package institution_gestion_notes.service;

public interface NoteService {
    double calculerMoyenneParEtudiant(Long idEtudiant);
    double calculerMoyenneParEtudiantEtSemestre(Long idEtudiant, int semestre);
}
