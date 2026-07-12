package net.watchbox.global.auth.oauth.custom;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.auth.dto.SocialUserInfo;
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
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

/**
 * 애플 웹 로그인(리다이렉트) 처리 로직. 컨트롤러는 얇게 두고 실제 흐름은 여기서 담당한다.
 *
 * <p>{@code response_type=code id_token} + {@code response_mode=form_post} 로 요청해
 * 콜백에서 id_token 을 직접 받아 {@link AppleNativeAuthService} 로 JWKS 검증한다
 * (토큰 교환·client_secret·.p8 불필요). state 쿠키로 CSRF 를 방지한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppleWebLoginService {
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

    /** 애플 인증 페이지 URL 을 만들고, CSRF 방지용 state 를 쿠키에 심는다. */
    public String createAuthorizeUrl(HttpServletResponse response) {
        String state = UUID.randomUUID().toString();
        addStateCookie(response, state);

        return UriComponentsBuilder.fromUriString(AUTHORIZE_ENDPOINT)
                .queryParam("client_id", webClientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code id_token")
                .queryParam("response_mode", "form_post")
                .queryParam("scope", "name email")
                .queryParam("state", state)
                .build().toUriString();
    }

    /**
     * 애플 form_post 콜백 처리. state 검증 → id_token 검증 → 회원 조회/생성 → 쿠키 발급.
     * @return 리다이렉트할 프론트 URL
     */
    public String handleCallback(String idToken, String state, String error,
                                 HttpServletRequest request, HttpServletResponse response) {
        clearStateCookie(response);

        if (error != null) {
            log.warn("[AppleWebLogin] 애플 콜백 error: {}", error);
            return withErrorParam(frontendCallback);
        }

        // CSRF: authorize 에서 심은 state 쿠키와 대조
        String cookieState = readCookie(request, STATE_COOKIE);
        if (cookieState == null || !cookieState.equals(state)) {
            throw new CustomException(ErrorCode.PARAMETER_BAD_REQUEST);
        }

        SocialUserInfo userInfo = appleNativeAuthService.verifyIdentityToken(idToken);
        OAuthAccount oauthAccount = oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.APPLE, userInfo);

        boolean isNewMember = authService.isNewMemberByOAuthAccount(oauthAccount);
        Member member = authService.findOrCreateMemberByOAuthAccount(oauthAccount);
        if (isNewMember) {
            boxService.createInitialMyBox(member);
        }

        authService.issueTokensAndSetCookies(member, request, response);
        return frontendCallback;
    }

    private void addStateCookie(HttpServletResponse response, String state) {
        // form_post 는 appleid.apple.com → 우리 도메인으로의 cross-site POST 라, state 쿠키가 실려오려면
        // SameSite=None; Secure 여야 한다. (그래서 Apple 웹 로그인은 https 환경에서만 동작)
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
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String withErrorParam(String url) {
        return UriComponentsBuilder.fromUriString(url)
                .queryParam("error", "apple_login_failed")
                .build().toUriString();
    }
}
