package institution_gestion_notes.controller;

import institution_gestion_notes.repository.*;
import institution_gestion_notes.entity.Etudiant;
import institution_gestion_notes.entity.Professeur;
import institution_gestion_notes.entity.Matiere;
import institution_gestion_notes.repository.EtudiantRepository;
import institution_gestion_notes.repository.ProfesseurRepository;
import institution_gestion_notes.repository.MatiereRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminWebController {

    private final EtudiantRepository etuRepo;
    private final ProfesseurRepository profRepo;
    private final MatiereRepository matRepo;
    private final UserRepository userRepo;

    public AdminWebController(EtudiantRepository etuRepo,
                              ProfesseurRepository profRepo,
                              MatiereRepository matRepo,
                              UserRepository userRepo) {
        this.etuRepo = etuRepo;
        this.profRepo = profRepo;
        this.matRepo = matRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Authentication authentication, Model model) {

        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", authentication.getAuthorities());

        // 🔹 Statistiques dynamiques
        model.addAttribute("totalEtudiants", etuRepo.count());
        model.addAttribute("totalProfesseurs", profRepo.count());
        model.addAttribute("totalMatieres", matRepo.count());
        model.addAttribute("totalUsers", userRepo.count());

        return "admin_dashboard";
    }

    @GetMapping("/admin/etudiants")
    public String etudiants(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", authentication.getAuthorities());
        model.addAttribute("etudiants", etuRepo.findAll());
        model.addAttribute("newEtudiant", new Etudiant());
        return "admin_etudiants";
    }

    @GetMapping("/admin/professeurs")
    public String professeurs(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", authentication.getAuthorities());
        model.addAttribute("professeurs", profRepo.findAll());
        model.addAttribute("newProfesseur", new Professeur());
        return "admin_professeurs";
    }
    

    @GetMapping("/admin/matieres")
    public String matieres(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", authentication.getAuthorities());
        model.addAttribute("matieres", matRepo.findAll());
        model.addAttribute("newMatiere", new Matiere());
        model.addAttribute("professeurs", profRepo.findAll()); // <--- Ajoute ceci
    return "admin_matieres";
}
}
