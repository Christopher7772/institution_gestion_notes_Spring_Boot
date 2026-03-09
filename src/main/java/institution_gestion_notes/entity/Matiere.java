package institution_gestion_notes.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "matieres")
public class Matiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_matiere")
    private Long idMatiere;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "code") // ❌ enlever unique ici
    private String code;

    @Column(name = "coefficient")
    private Integer coefficient = 1; // valeur par défaut

    @ManyToMany(mappedBy = "matieres")
    private Set<Etudiant> etudiants = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_professeur", nullable = true)
    private Professeur professeur;

    public Matiere() {}
    // getters / setters identiques

    public Long getIdMatiere() { return idMatiere; }
    public void setIdMatiere(Long idMatiere) { this.idMatiere = idMatiere; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getCoefficient() { return coefficient; }
    public void setCoefficient(Integer coefficient) { this.coefficient = coefficient; }

    public Set<Etudiant> getEtudiants() { return etudiants; }
    public void setEtudiants(Set<Etudiant> etudiants) { this.etudiants = etudiants != null ? etudiants : new HashSet<>(); }

    public Professeur getProfesseur() { return professeur; }
    public void setProfesseur(Professeur professeur) { this.professeur = professeur; }
}
