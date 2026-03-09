package institution_gestion_notes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        // Récupère le token CSRF (fourni par Spring Security si CSRF activé)
        CsrfToken csrf = (CsrfToken) request.getAttribute("_csrf");
        if (csrf != null) {
            model.addAttribute("_csrf", csrf);
        }
        return "login"; // rend login.html dans templates/
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    // Optionnel : dashboard pour redirect après succès
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // crée dashboard.html minimal si nécessaire
    }
}
