package institution_gestion_notes.controller;

import institution_gestion_notes.dto.RegisterRequest;
import institution_gestion_notes.entity.Etudiant;
import institution_gestion_notes.entity.Professeur;
import institution_gestion_notes.entity.Role;
import institution_gestion_notes.entity.User;
import institution_gestion_notes.repository.EtudiantRepository;
import institution_gestion_notes.repository.ProfesseurRepository;
import institution_gestion_notes.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import institution_gestion_notes.dto.ForgotPasswordRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final EtudiantRepository etudiantRepository;
    private final ProfesseurRepository professeurRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          EtudiantRepository etudiantRepository,
                          ProfesseurRepository professeurRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.etudiantRepository = etudiantRepository;
        this.professeurRepository = professeurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest request, Model model) {

        // 1. Vérifications de base (Mots de passe et Email)
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "Les mots de passe ne correspondent pas !");
            return "register";
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            model.addAttribute("error", "Email déjà utilisé !");
            return "register";
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        // 2. Vérification PRO : L'utilisateur existe-t-il dans les dossiers de l'admin ?
        if ("STUDENT".equals(request.getRole())) {
            Optional<Etudiant> etudiantOpt = etudiantRepository.findByNomIgnoreCaseAndPrenomIgnoreCase(request.getNom(), request.getPrenom());
            
            if (etudiantOpt.isEmpty()) {
                model.addAttribute("error", "Aucun étudiant trouvé avec ce nom et prénom. Veuillez contacter l'administration.");
                return "register";
            }
            
            Etudiant etudiant = etudiantOpt.get();
            if (etudiant.getUser() != null) {
                model.addAttribute("error", "Ce profil étudiant a déjà été activé par un autre compte.");
                return "register";
            }

            user.setRole(Role.STUDENT);
            userRepository.save(user); // On sauvegarde le User d'abord
            
            etudiant.setUser(user); // On fait la liaison
            etudiantRepository.save(etudiant); // On met à jour l'étudiant

        } else if ("TEACHER".equals(request.getRole())) {
            Optional<Professeur> profOpt = professeurRepository.findByNomIgnoreCaseAndPrenomIgnoreCase(request.getNom(), request.getPrenom());
            
            if (profOpt.isEmpty()) {
                model.addAttribute("error", "Aucun professeur trouvé avec ce nom et prénom.");
                return "register";
            }
            
            Professeur prof = profOpt.get();
            if (prof.getUser() != null) {
                model.addAttribute("error", "Ce profil professeur a déjà été activé.");
                return "register";
            }

            user.setRole(Role.TEACHER);
            userRepository.save(user);
            
            prof.setUser(user);
            professeurRepository.save(prof);
            
        // --- DÉBUT DE L'AJOUT POUR L'ADMIN ---
        } else if ("ADMIN".equals(request.getRole())) {
            
            user.setRole(Role.ADMIN);
            // On sauvegarde directement l'utilisateur avec les droits Admin
            userRepository.save(user);
            
        // --- FIN DE L'AJOUT ---

        } else {
            model.addAttribute("error", "Veuillez sélectionner un statut valide.");
            return "register";
        }
        return "redirect:/login?registered";
    }

    // --- FORGOT PASSWORD (Inchangé) ---
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String resetPassword(@ModelAttribute ForgotPasswordRequest request, Model model) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "Les mots de passe ne correspondent pas !");
            return "forgot-password";
        }
        var userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "Email introuvable !");
            return "forgot-password";
        }
        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "redirect:/login?passwordChanged";
    }
}