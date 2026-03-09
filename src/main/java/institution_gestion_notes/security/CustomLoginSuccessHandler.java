package institution_gestion_notes.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        authentication.getAuthorities().forEach(authority -> {
            try {
                String role = authority.getAuthority();

                if (role.equals("ROLE_ADMIN")) {
                    response.sendRedirect("/admin/dashboard");
                } else if (role.equals("ROLE_TEACHER")) {
                    response.sendRedirect("/teacher/dashboard");
                } else if (role.equals("ROLE_STUDENT")) {
                    response.sendRedirect("/student/dashboard");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
