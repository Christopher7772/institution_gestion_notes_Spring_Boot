package institution_gestion_notes.controller;

import institution_gestion_notes.entity.Etudiant;
import institution_gestion_notes.entity.Note;
import institution_gestion_notes.repository.EtudiantRepository;
import institution_gestion_notes.repository.NoteRepository;
import institution_gestion_notes.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.context.Context;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentViewController {

    private final EtudiantRepository etudiantRepository;
    private final NoteRepository noteRepository;
    private final PdfService pdfService;
    
    public StudentViewController(EtudiantRepository etudiantRepository, 
                                 NoteRepository noteRepository, 
                                 PdfService pdfService) {
        this.etudiantRepository = etudiantRepository;
        this.noteRepository = noteRepository;
        this.pdfService = pdfService;
    }

    // 1. DASHBOARD (Mon bulletin simplifié)
    @GetMapping("/dashboard")
    public String studentDashboard(Principal principal, Authentication auth, Model model) {
        addCommonAttrs(principal, auth, model);
        Etudiant etudiant = getAuthenticatedEtudiant(principal);

        if (etudiant == null) return "student_dashboard";

        List<Note> notes = noteRepository.findByEtudiantIdEtudiantAndSemestre(etudiant.getIdEtudiant(), 1);
        model.addAttribute("notes", notes);
        
        // Utilisation de la logique de calcul pondérée par les coefficients de l'admin
        double moyenne = calculerMoyennePonderee(notes);
        model.addAttribute("moyenne", moyenne);

        return "student_dashboard";
    }

    // 2. MES NOTES (Tableau détaillé connecté aux matières admin)
    @GetMapping("/notes")
    public String studentNotes(Principal principal, Authentication auth, Model model) {
        addCommonAttrs(principal, auth, model);
        Etudiant etudiant = getAuthenticatedEtudiant(principal);

        if (etudiant == null) return "student_notes";

        // Récupération des notes (qui contiennent les relations Matiere -> Professeur)
        List<Note> notes = noteRepository.findByEtudiantIdEtudiantAndSemestre(etudiant.getIdEtudiant(), 1);
        model.addAttribute("notes", notes);

        // Moyenne pro basée sur les coefficients
        double moyenne = calculerMoyennePonderee(notes);
        model.addAttribute("moyenne", moyenne);

        // Calcul du total des points (Note * Coeff)
        double totalPoints = notes.stream()
                .mapToDouble(n -> n.getValeur() * n.getMatiere().getCoefficient())
                .sum();
        model.addAttribute("totalPoints", Math.round(totalPoints * 100.0) / 100.0);

        return "student_notes";
    }

    // 3. PAGE DE TÉLÉCHARGEMENT
    @GetMapping("/bulletin")
    public String studentBulletinPage(Principal principal, Authentication auth, Model model) {
        addCommonAttrs(principal, auth, model);
        return "student_bulletin";
    }

    // 4. ACTION DE GÉNÉRATION DU PDF
    @GetMapping("/download-bulletin")
    public ResponseEntity<byte[]> downloadBulletin(Principal principal) {
        Etudiant etudiant = getAuthenticatedEtudiant(principal);
        if (etudiant == null) return ResponseEntity.notFound().build();

        List<Note> notes = noteRepository.findByEtudiantIdEtudiantAndSemestre(etudiant.getIdEtudiant(), 1);
        double moyenne = calculerMoyennePonderee(notes);

        Context context = new Context();
        context.setVariable("etudiant", etudiant);
        context.setVariable("notes", notes);
        context.setVariable("moyenne", moyenne);

        byte[] pdfBytes = pdfService.genererBulletinPdf("pdf/bulletin_pdf", context);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Bulletin_" + etudiant.getNom() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // --- MÉTHODES PRIVÉES DE LOGIQUE MÉTIER ---

    private double calculerMoyennePonderee(List<Note> notes) {
        if (notes == null || notes.isEmpty()) return 0.0;
        
        double totalPoints = notes.stream()
                .mapToDouble(n -> n.getValeur() * n.getMatiere().getCoefficient())
                .sum();
        
        int totalCoeffs = notes.stream()
                .mapToInt(n -> n.getMatiere().getCoefficient())
                .sum();

        double moyenne = (totalCoeffs > 0) ? (totalPoints / totalCoeffs) : 0.0;
        return Math.round(moyenne * 100.0) / 100.0;
    }

    private Etudiant getAuthenticatedEtudiant(Principal principal) {
        if (principal == null) return null;
        return etudiantRepository.findByUserEmail(principal.getName()).orElse(null);
    }

    private void addCommonAttrs(Principal principal, Authentication auth, Model model) {
        model.addAttribute("username", principal != null ? principal.getName() : "Invité");
        if (auth != null) {
            String roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", "));
            model.addAttribute("roles", roles);
        }
    }
}