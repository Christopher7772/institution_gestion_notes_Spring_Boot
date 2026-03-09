package institution_gestion_notes.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
// IMPORTANT : Ajoute cet import !
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "etudiants")
public class Etudiant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etudiant")
    private Long idEtudiant;

    @Column(name = "code_etudiant", unique = true)
    private String codeEtudiant;

    @Column(name = "numero_etudiant", unique = true)
    private String numeroEtudiant;

    private String nom;
    private String prenom;
    private String classe;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User user;

    // SOLUTION ICI : Empêche Spring de boucler à l'infini lors de la conversion en JSON
    @JsonIgnore 
    @ManyToMany
    @JoinTable(name = "etudiant_matieres",
            joinColumns = @JoinColumn(name = "id_etudiant"),
            inverseJoinColumns = @JoinColumn(name = "id_matiere"))
    private Set<Matiere> matieres = new HashSet<>();

    public Etudiant() {}

    // ... (Reste de tes getters et setters identiques)
    public Long getIdEtudiant() { return idEtudiant; }
    public void setIdEtudiant(Long idEtudiant) { this.idEtudiant = idEtudiant; }
    public String getCodeEtudiant() { return codeEtudiant; }
    public void setCodeEtudiant(String codeEtudiant) { this.codeEtudiant = codeEtudiant; }
    public String getNumeroEtudiant() { return numeroEtudiant; }
    public void setNumeroEtudiant(String numeroEtudiant) { this.numeroEtudiant = numeroEtudiant; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Set<Matiere> getMatieres() { return matieres; }
    public void setMatieres(Set<Matiere> matieres) {
        this.matieres = matieres != null ? matieres : new HashSet<>();
    }
}