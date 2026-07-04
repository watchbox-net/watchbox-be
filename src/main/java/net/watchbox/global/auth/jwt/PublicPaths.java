package net.watchbox.global.auth.jwt;

import java.util.List;
import java.util.stream.Stream;

/**
 * 인증 불필요 경로를 한 곳에서 관리.
 * - TokenAuthenticationFilter: PREFIXES로 startsWith 매칭
 * - WebSecurityConfig: antPatterns()로 requestMatchers에 사용
 */
public final class PublicPaths {

    private PublicPaths() {}

    /** 인증 불필요 — 토큰 검증 자체를 스킵 (TokenAuthenticationFilter) */
    public static final List<String> SKIP_TOKEN_PREFIXES = List.of(
            "/health",
            "/dev/",
            "/api/search",
            "/api/preview/",
            "/api/auth/",
            "/api/admin/",
            "/swagger-ui/",
            "/v3/api-docs/"
    );

    /** SKIP_TOKEN_PREFIXES 에 매칭되더라도 예외적으로 인증이 필요한 경로.
     *  예: /api/auth/ 는 스킵 대상이지만 /api/auth/logout 은 @AuthenticationPrincipal 로 회원을 식별해야 한다. */
    public static final List<String> AUTH_REQUIRED_EXCEPTIONS = List.of(
            "/api/auth/logout"
    );

    /** 비로그인도 접근 가능 — 토큰 있으면 인증 설정, 없으면 비로그인으로 통과 */
    public static final List<String> OPTIONAL_AUTH_PREFIXES = List.of(
            "/api/discover/",
            "/api/contents/"
    );

    /** Spring Security requestMatchers 용 Ant 패턴 (permitAll 대상 전체) */
    public static String[] antPatterns() {
        return Stream.concat(
                SKIP_TOKEN_PREFIXES.stream(),
                OPTIONAL_AUTH_PREFIXES.stream()
        ).map(p -> p.endsWith("/") ? p + "**" : p + "/**").toArray(String[]::new);
    }

    /** 인증 필요 예외 경로들의 requestMatchers 용 배열 (permitAll 보다 먼저 authenticated 로 등록). */
    public static String[] authRequiredExceptionPatterns() {
        return AUTH_REQUIRED_EXCEPTIONS.toArray(String[]::new);
    }
}
