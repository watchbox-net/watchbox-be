package net.watchbox.global.auth.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.auth.entity.OAuthAccount;
import net.watchbox.domain.auth.entity.OAuthProvider;
import net.watchbox.domain.auth.service.AppleNativeAuthService;
import net.watchbox.domain.auth.service.AuthService;
import net.watchbox.domain.auth.service.OAuthAccountService;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

/**
 * 애플 웹 로그인(리다이렉트) — 커스텀 구현.
 *
 * <p>Spring Security oauth2Login 은 static client-secret 을 요구하지만 Apple 은 client_secret 이
 * ES256 서명 JWT(만료 있음)라 맞지 않는다. 그래서 등록 대신 커스텀 컨트롤러로 처리하고,
 * {@code response_type=code id_token} + {@code response_mode=form_post} 로 요청해
 * 콜백에서 <b>id_token 을 직접 받아 JWKS 로 검증</b>한다(토큰 교환·client_secret·.p8 불필요).
 * 검증은 네이티브와 동일한 {@link AppleNativeAuthService} 를 재사용한다(aud 에 Services ID 포함 필요).
 *
 * <p>경로는 Spring Security 의 {@code /oauth2/authorization/*}, {@code /login/oauth2/code/*}
 * 기본 매처와 겹치지 않도록 {@code /oauth2/apple/*} 를 쓴다.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class AppleWebLoginController {
    private static final String AUTHORIZE_ENDPOINT = "https://appleid.apple.com/auth/authorize";
    private static final String STATE_COOKIE = "apple_oauth_state";

    @Value("${apple.web.client-id}")
    private String webClientId;

    @Value("${apple.web.redirect-uri}")
    private String redirectUri;

    // 로그인 성사 후 이동할 프론트 콜백(구글과 동일 값 재사용).
    @Value("${url.oauth-callback}")
    private String frontendCallback;

    private final AppleNativeAuthService appleNativeAuthService;
    private final OAuthAccountService oAuthAccountService;
    private final AuthService authService;
    private final BoxService boxService;

    /** 애플 인증 페이지로 리다이렉트. CSRF 방지용 state 를 쿠키에 심는다. */
    @GetMapping("/oauth2/apple/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        // form_post 는 appleid.apple.com → 우리 도메인으로의 cross-site POST 라, state 쿠키가 실려오려면
        // SameSite=None; Secure 여야 한다. (그래서 Apple 웹 로그인은 https 환경에서만 동작)
        addStateCookie(response, state);

        String url = UriComponentsBuilder.fromUriString(AUTHORIZE_ENDPOINT)
                .queryParam("client_id", webClientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code id_token")
                .queryParam("response_mode", "form_post")
                .queryParam("scope", "name email")
                .queryParam("state", state)
                .build().toUriString();

        response.sendRedirect(url);
    }

    /** 애플이 form_post 로 보내는 콜백. id_token 검증 → 회원 조회/생성 → 쿠키 발급 → 프론트로 리다이렉트. */
    @PostMapping("/oauth2/apple/callback")
    public void callback(
            @RequestParam(name = "id_token", required = false) String idToken,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        clearStateCookie(response);

        if (error != null) {
            log.warn("[AppleWebLogin] 애플 콜백 error: {}", error);
            response.sendRedirect(withErrorParam(frontendCallback));
            return;
        }

        // CSRF: authorize 에서 심은 state 쿠키와 대조
        String cookieState = readCookie(request, STATE_COOKIE);
        if (cookieState == null || !cookieState.equals(state)) {
            throw new CustomException(ErrorCode.PARAMETER_BAD_REQUEST);
        }

        AppleNativeAuthService.AppleUserInfo userInfo = appleNativeAuthService.verifyIdentityToken(idToken);
        OAuthAccount oauthAccount = oAuthAccountService.findOrCreateNativeAccount(
                OAuthProvider.APPLE, userInfo.sub(), userInfo.email(), userInfo.name());

        boolean isNewMember = authService.isNewMemberByOAuthAccount(oauthAccount);
        Member member = authService.findOrCreateMemberByOAuthAccount(oauthAccount);
        if (isNewMember) {
            boxService.createInitialMyBox(member);
        }

        authService.issueTokensAndSetCookies(member, request, response);
        response.sendRedirect(frontendCallback);
    }

    private void addStateCookie(HttpServletResponse response, String state) {
        ResponseCookie cookie = ResponseCookie.from(STATE_COOKIE, state)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMinutes(5))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearStateCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(STATE_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String withErrorParam(String url) {
        return UriComponentsBuilder.fromUriString(url)
                .queryParam("error", "apple_login_failed")
                .build().toUriString();
    }
}
