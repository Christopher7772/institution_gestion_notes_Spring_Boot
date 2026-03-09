/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package institution_gestion_notes.dto;



public class MatiereDTO {

    private Long id;
    private String nom;
    private Integer coefficient;
    private boolean assigned;

    public MatiereDTO(Long id, String nom, Integer coefficient, boolean assigned) {
        this.id = id;
        this.nom = nom;
        this.coefficient = coefficient;
        this.assigned = assigned;
    }

    // getters
public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getCoefficient() { return coefficient; }
    public void setCoefficient(int coefficient) { this.coefficient = coefficient; }

    public boolean getAssigned() { return assigned; }
    public void setAssigned(boolean assigned) { this.assigned = assigned; }
}
