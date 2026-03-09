package institution_gestion_notes.repository;

import institution_gestion_notes.entity.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    Optional<Etudiant> findByCodeEtudiant(String codeEtudiant);
    Optional<Etudiant> findByNumeroEtudiant(String numeroEtudiant);

    // utile : retrouver l'étudiant via l'email de son user lié
    Optional<Etudiant> findByUserEmail(String email);
    
    // AJOUT PRO : Trouver les étudiants inscrits à une matière précise
    List<Etudiant> findByMatieresIdMatiere(Long idMatiere);
    
    Optional<Etudiant> findByNomIgnoreCaseAndPrenomIgnoreCase(String nom, String prenom);
    
    
}