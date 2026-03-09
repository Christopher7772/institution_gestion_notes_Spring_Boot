/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package institution_gestion_notes.dto;

public class NoteDTO {

    private String matiere;
    private Double valeur;
    private Integer semestre;

    public NoteDTO(String matiere, Double valeur, Integer semestre) {
        this.matiere = matiere;
        this.valeur = valeur;
        this.semestre = semestre;
    }

    // getters
  public String getMatiere() { return matiere; }
    public double getValeur() { return valeur; }
    public int getSemestre() { return semestre; }
    
    public void setMatiere(String  m) { this.matiere = m; }
    public void setValeur(double v) { this.valeur = v; }
    public void setSemestre(int s) { this.semestre = s; }
    
   
   
}
