package institution_gestion_notes.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer; 
import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    // 1. Déclarer la variable en 'final' est une bonne pratique
    private final TemplateEngine templateEngine;

    // 2. AJOUTER CE CONSTRUCTEUR (Indispensable pour corriger le NullPointerException)
    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] genererBulletinPdf(String templateName, Context context) {
        try {
            // 3. Rendre le template HTML en String
            // Grâce au constructeur au-dessus, templateEngine n'est plus null !
            String htmlContent = templateEngine.process(templateName, context);
            
            // 4. Préparer la sortie PDF
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ITextRenderer renderer = new ITextRenderer();
                
                // Flying Saucer a besoin de HTML bien formé (XHTML)
                renderer.setDocumentFromString(htmlContent);
                renderer.layout();
                renderer.createPDF(outputStream);
                
                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            // Affiche l'erreur précise dans la console si le PDF échoue
            System.err.println("Erreur lors de la génération du PDF : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}