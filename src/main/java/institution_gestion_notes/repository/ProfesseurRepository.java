package institution_gestion_notes.repository;

import institution_gestion_notes.entity.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfesseurRepository extends JpaRepository<Professeur, Long> {
    Optional<Professeur> findByNumeroProfesseur(String numeroProfesseur);

    // utile : retrouver le professeur via l'email associé au User
    Optional<Professeur> findByUserEmail(String email);
    
    Optional<Professeur> findByNomIgnoreCaseAndPrenomIgnoreCase(String nom, String prenom);
}
