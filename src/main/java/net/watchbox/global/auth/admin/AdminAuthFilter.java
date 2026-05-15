package net.watchbox.global.auth.admin;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.watchbox.global.properties.AdminProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class AdminAuthFilter extends OncePerRequestFilter {

    private static final String ADMIN_PATH_PREFIX = "/api/admin";
    private static final String HEADER_NAME = "X-Admin-Key";

    private final AdminProperties adminProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(ADMIN_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String configured = adminProperties.getApiKey();
        String provided = request.getHeader(HEADER_NAME);

        if (configured == null || configured.isBlank() || !configured.equals(provided)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"code\":\"ADMIN-401\",\"message\":\"관리자 인증 실패\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
