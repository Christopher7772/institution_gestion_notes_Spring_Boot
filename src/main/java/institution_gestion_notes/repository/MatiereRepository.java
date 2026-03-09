package institution_gestion_notes.repository;

import institution_gestion_notes.entity.Matiere;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.jpa.repository.Query;

public interface MatiereRepository extends JpaRepository<Matiere, Long> {
    
    // interface MatiereRepository extends JpaRepository<Matiere, Long>
@Query("SELECT m FROM Matiere m LEFT JOIN FETCH m.professeur")
List<Matiere> findAllWithProfesseur();
    List<Matiere> findByProfesseurIdProfesseur(Long idProfesseur);
    List<Matiere> findAll(); 
    List<Matiere> findAllById(Iterable<Long> ids); 
    boolean existsByNom(String nom);

    boolean existsByCode(String code);
}
