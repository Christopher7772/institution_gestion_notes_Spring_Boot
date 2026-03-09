package institution_gestion_notes.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "professeurs")
public class Professeur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_professeur")
    private Long idProfesseur;

    @Column(name = "numero_professeur", unique = true)
    private String numeroProfesseur;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;
    
    @Column(name = "specialite")
    private String specialite;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User user;

    public Professeur() {}

    // Getters & Setters
    public Long getIdProfesseur() { return idProfesseur; }
    public void setIdProfesseur(Long idProfesseur) { this.idProfesseur = idProfesseur; }

    public String getNumeroProfesseur() { return numeroProfesseur; }
    public void setNumeroProfesseur(String numeroProfesseur) { this.numeroProfesseur = numeroProfesseur; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    

    // Et le getter correspondant
   public String getSpecialite() {return specialite;}
   public void setSpecialite(String specialite) { this.specialite = specialite; }
 
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    /**
     * Helper pratique pour récupérer l'email du professeur (via le User lié).
     * Retourne null si aucun user lié.
     */
    public String getEmail() {
        return (user != null) ? user.getEmail() : null;
    }
    
    public void setEmail(String email) {
    if (this.user != null) {
        this.user.setEmail(email);
    }
    // Note : si 'user' est null, l'email ne sera pas mis à jour.
    // Dans ton système, un professeur a normalement toujours un compte User lié.
}
}
