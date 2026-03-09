package institution_gestion_notes.controller;

import institution_gestion_notes.entity.Matiere;
import institution_gestion_notes.entity.Note;
import institution_gestion_notes.entity.Professeur;
import institution_gestion_notes.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher")
public class TeacherViewController {

    private final ProfesseurRepository professeurRepository;
    private final MatiereRepository matiereRepository;
    private final NoteRepository noteRepository;

    public TeacherViewController(ProfesseurRepository professeurRepository,
                                 MatiereRepository matiereRepository,
                                 NoteRepository noteRepository) {
        this.professeurRepository = professeurRepository;
        this.matiereRepository = matiereRepository;
        this.noteRepository = noteRepository;
    }

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(Principal principal, Authentication auth, Model model) {
        addCommonAttributes(principal, auth, model);

        model.addAttribute("matieres", new ArrayList<>());
        model.addAttribute("recentNotes", new ArrayList<>());
        model.addAttribute("studentsCount", 0L);
        model.addAttribute("matieresCount", 0);

        if (principal == null) return "teacher_dashboard";

        professeurRepository.findByUserEmail(principal.getName()).ifPresent(prof -> {
            List<Matiere> matieres = matiereRepository.findByProfesseurIdProfesseur(prof.getIdProfesseur());
            
            if (matieres != null) {
                // Force le chargement des étudiants pour calculer le VRAI total d'inscrits
                matieres.forEach(m -> { if (m.getEtudiants() != null) m.getEtudiants().size(); });
                model.addAttribute("matieres", matieres);
                model.addAttribute("matieresCount", matieres.size());

                // Calcul du nombre total d'étudiants uniques inscrits aux cours de ce prof
                long studentsCount = matieres.stream()
                        .filter(m -> m.getEtudiants() != null)
                        .flatMap(m -> m.getEtudiants().stream())
                        .distinct()
                        .count();
                model.addAttribute("studentsCount", studentsCount);
            }

            List<Note> allNotes = noteRepository.findByProfesseurIdProfesseur(prof.getIdProfesseur());
            if (allNotes != null) {
                List<Note> recentNotes = allNotes.stream()
                        .filter(n -> n != null && n.getEtudiant() != null && n.getMatiere() != null)
                        .sorted(Comparator.comparing(Note::getIdNote).reversed())
                        .limit(5)
                        .collect(Collectors.toList());
                model.addAttribute("recentNotes", recentNotes);
            }
        });

        return "teacher_dashboard";
    }
@GetMapping("/matieres")
@Transactional(readOnly = true)
public String mesMatieres(Principal principal, Authentication auth, Model model) {
    addCommonAttributes(principal, auth, model);
    
    // Initialisation vitale pour éviter l'erreur 500 si le HTML cherche ces variables
    model.addAttribute("matieres", new ArrayList<>());
    model.addAttribute("matieresCount", 0);
    model.addAttribute("studentsCount", 0L);
    
    if (principal != null) {
        professeurRepository.findByUserEmail(principal.getName()).ifPresent(prof -> {
            List<Matiere> matieres = matiereRepository.findByProfesseurIdProfesseur(prof.getIdProfesseur());
            if (matieres != null) {
                // On force le chargement pour l'affichage
                matieres.forEach(m -> { if(m.getEtudiants() != null) m.getEtudiants().size(); });
                model.addAttribute("matieres", matieres);
                model.addAttribute("matieresCount", matieres.size());

                // Calcul du nombre d'étudiants (pour que ta sidebar affiche "3" et pas "0" ou rien)
                long count = matieres.stream()
                        .filter(m -> m.getEtudiants() != null)
                        .flatMap(m -> m.getEtudiants().stream())
                        .distinct()
                        .count();
                model.addAttribute("studentsCount", count);
            }
        });
    }
    return "teacher_matieres";
}

    @GetMapping("/notes")
    @Transactional(readOnly = true)
    public String saisirNotes(Principal principal, Authentication auth, Model model) {
        addCommonAttributes(principal, auth, model);
        
        model.addAttribute("matieres", new ArrayList<>());
        model.addAttribute("matieresCount", 0);
        model.addAttribute("studentsCount", 0L);
        
        if (principal != null) {
            professeurRepository.findByUserEmail(principal.getName()).ifPresent(prof -> {
                List<Matiere> matieres = matiereRepository.findByProfesseurIdProfesseur(prof.getIdProfesseur());
                
                if (matieres != null) {
                    model.addAttribute("matieres", matieres);
                    model.addAttribute("matieresCount", matieres.size());

                    long studentsCount = matieres.stream()
                            .filter(m -> m.getEtudiants() != null)
                            .flatMap(m -> m.getEtudiants().stream())
                            .distinct()
                            .count();
                    model.addAttribute("studentsCount", studentsCount);
                }
            });
        }
        return "teacher_notes_saisie";
    }

    private void addCommonAttributes(Principal principal, Authentication auth, Model model) {
        model.addAttribute("username", principal != null ? principal.getName() : "Invité");
        String roles = (auth != null) ? auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", ")) : "";
        model.addAttribute("roles", roles);
    }
}