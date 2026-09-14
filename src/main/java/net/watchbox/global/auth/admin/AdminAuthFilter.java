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
import java.util.List;

@RequiredArgsConstructor
public class AdminAuthFilter extends OncePerRequestFilter {

    /**
     * X-Admin-Key 를 요구하는 경로.
     *
     * <p>{@code /dev/}, {@code /health} 는 스웨거에 공개하되 호출은 막는다. 문서에 보이는 것과
     * 실행할 수 있는 것은 다른 문제다 — 포트폴리오로 보여주려면 목록은 열려 있어야 하고,
     * 테스트 계정 로그인·Redis 쓰기·전송 경로 전환은 아무나 부르면 안 된다.
     *
     * <p>액추에이터는 {@code /actuator/health} 라 이 접두어에 걸리지 않는다.
     */
    private static final List<String> GUARDED_PREFIXES = List.of("/api/admin", "/dev/", "/health");
    private static final String HEADER_NAME = "X-Admin-Key";

    private final AdminProperties adminProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return GUARDED_PREFIXES.stream().noneMatch(uri::startsWith);
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
