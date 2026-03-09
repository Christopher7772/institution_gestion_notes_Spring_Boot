package institution_gestion_notes.service;

import institution_gestion_notes.entity.Etudiant;
import java.util.Optional;

public interface EtudiantService {
    Etudiant createEtudiant(Etudiant etudiant);
    Optional<Etudiant> findById(Long idEtudiant);
}
