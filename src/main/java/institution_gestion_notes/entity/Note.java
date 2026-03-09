package institution_gestion_notes.entity;

import jakarta.persistence.*;

// Note.java (extrait)
@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNote;

    private double valeur;
    private int semestre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etudiant")
    private Etudiant etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_matiere")
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_professeur")
    private Professeur professeur;

    // getters / setters (important)
    public Long getIdNote() { return idNote; }
    // Ajoute ceci dans ton fichier Note.java
public void setIdNote(Long idNote) {
    this.idNote = idNote;
}
    public double getValeur() { return valeur; }
    public int getSemestre() { return semestre; }
    public Etudiant getEtudiant() { return etudiant; }
    public Matiere getMatiere() { return matiere; }
    public Professeur getProfesseur() { return professeur; }

    public void setValeur(double v) { this.valeur = v; }
    public void setSemestre(int s) { this.semestre = s; }
    public void setEtudiant(Etudiant e) { this.etudiant = e; }
    public void setMatiere(Matiere m) { this.matiere = m; }
    public void setProfesseur(Professeur p) { this.professeur = p; }
}
