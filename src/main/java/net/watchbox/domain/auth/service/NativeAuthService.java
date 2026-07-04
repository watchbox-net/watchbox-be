package net.watchbox.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.auth.dto.TokenRefreshResponse;
import net.watchbox.domain.auth.entity.OAuthAccount;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.properties.CookieProperties;
import net.watchbox.global.properties.JwtProperties;
import net.watchbox.global.util.HttpRequestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 네이티브 로그인 / 기존 redirect OAuth 공통 로직.
 * - 회원 조회·생성 (OAuthAccount → Member)
 * - JWT 발급 + Redis 저장 + HttpOnly 쿠키 설정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NativeAuthService {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final MemberService memberService;
    private final OAuthAccountService oAuthAccountService;
    private final BoxService boxService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenSessionService refreshTokenSessionService;
    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

    public Member findOrCreateMember(OAuthAccount oauthAccount) {
        if (memberService.notExistsByOauthAccount(oauthAccount)) {
            log.info("[NativeAuth] 신규 회원 가입 - email: {}", oauthAccount.getEmail());
            Member member = memberService.createMember(oauthAccount);
            oAuthAccountService.linkMember(oauthAccount, member);
            boxService.createInitialMyBox(member);
            return member;
        }
        log.info("[NativeAuth] 기존 회원 로그인 - email: {}", oauthAccount.getEmail());
        return memberService.getByOauthAccount(oauthAccount);
    }

    public void issueTokensAndSetCookies(Member member, HttpServletRequest request, HttpServletResponse response) {
        TokenRefreshResponse tokens = issueTokens(member, request);
        addTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME, tokens.getAccessToken(), jwtProperties.refreshTokenExpiry());
        addTokenCookie(response, REFRESH_TOKEN_COOKIE_NAME, tokens.getRefreshToken(), jwtProperties.refreshTokenExpiry());
    }

    /**
     * 토큰을 발급(+Redis 세션 저장)하되 쿠키는 심지 않고 값만 반환한다.
     * 하이브리드 로그인(localhost)에서 프론트가 응답 바디로 토큰을 받아 직접 쿠키를 심는 용도.
     */
    public TokenRefreshResponse issueTokens(Member member, HttpServletRequest request) {
        String ip = HttpRequestUtils.extractClientIp(request);
        String deviceInfo = request.getHeader("User-Agent");

        String refreshToken = tokenProvider.generateToken(member, jwtProperties.refreshTokenExpiry());
        refreshTokenSessionService.save(member, refreshToken, ip, deviceInfo);
        String accessToken = tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry());

        return new TokenRefreshResponse(accessToken, refreshToken);
    }

    private void addTokenCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/")
                .maxAge(maxAge);
        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isBlank()) {
            builder.domain(cookieProperties.getDomain());
        }
        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }
}
