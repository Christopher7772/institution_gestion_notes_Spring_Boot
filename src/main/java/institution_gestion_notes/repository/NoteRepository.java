package institution_gestion_notes.repository;

import institution_gestion_notes.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    
    java.util.Optional<Note> findByEtudiantIdEtudiantAndMatiereIdMatiereAndSemestre(Long idEtudiant, Long idMatiere, int semestre);
    // notes d'un professeur via l'id du professeur
    List<Note> findByProfesseurIdProfesseur(Long idProfesseur);

    // notes par email professeur
    @Query("select n from Note n where n.professeur.user.email = :email")
    List<Note> findByProfesseurUserEmail(@Param("email") String email);

    // notes d'un étudiant pour un semestre
    List<Note> findByEtudiantIdEtudiantAndSemestre(Long idEtudiant, int semestre);
     List<Note> findByEtudiant_IdEtudiant(Long idEtudiant);
    // ✅ AJOUTE CETTE LIGNE
    List<Note> findByMatiereIdMatiere(Long idMatiere);

    // moyenne globale
    @Query("select avg(n.valeur) from Note n where n.etudiant.idEtudiant = :idEtudiant")
    Double averageByEtudiant(@Param("idEtudiant") Long idEtudiant);

    @Query("select avg(n.valeur) from Note n where n.etudiant.idEtudiant = :idEtudiant and n.semestre = :semestre")
    Double averageByEtudiantAndSemestre(@Param("idEtudiant") Long idEtudiant, @Param("semestre") int semestre);
}
