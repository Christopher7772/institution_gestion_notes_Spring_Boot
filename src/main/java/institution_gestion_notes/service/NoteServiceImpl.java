package institution_gestion_notes.service;

import institution_gestion_notes.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    public NoteServiceImpl(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public double calculerMoyenneParEtudiant(Long idEtudiant) {
        Double avg = noteRepository.averageByEtudiant(idEtudiant);
        return avg == null ? 0.0 : avg;
    }

    @Override
    @Transactional(readOnly = true)
    public double calculerMoyenneParEtudiantEtSemestre(Long idEtudiant, int semestre) {
        Double avg = noteRepository.averageByEtudiantAndSemestre(idEtudiant, semestre);
        return avg == null ? 0.0 : avg;
    }
}
