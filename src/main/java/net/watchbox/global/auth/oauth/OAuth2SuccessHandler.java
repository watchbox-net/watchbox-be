package net.watchbox.global.auth.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.auth.entity.OAuthAccount;
import net.watchbox.domain.auth.service.OAuthAccountService;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.auth.service.RefreshTokenSessionService;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.properties.CookieProperties;
import net.watchbox.global.properties.JwtProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler { // 인증 성공시 실행할 핸들러
    @Value("${url.oauth-callback}")
    private String REDIRECT_PATH;

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final TokenProvider tokenProvider;
    private final RefreshTokenSessionService refreshTokenSessionService;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final MemberService memberService;
    private final OAuthAccountService oAuthAccountService;
    private final BoxService boxService;
    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // OAuth2UserCustomService의 loadUser()에서 반환한 OAuth2User 사용
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // CustomOAuth2User로 캐스팅
        if (!(oAuth2User instanceof CustomOAuth2User)) {
            throw new IllegalArgumentException("OAuth2User is not an instance of CustomOAuth2User");
        }
        CustomOAuth2User customUser = (CustomOAuth2User) oAuth2User;
        OAuthAccount oauthAccount = customUser.getOauthAccount();

        /**
         * 최초 가입시
         * 1. Member 생성
         * 2. OAuthAccount에 Member 매핑
         * 3. MyBox 생성
         * 4. BoxMember Owner 생성
         */
        Member member;
        if(memberService.notExistsByOauthAccount(oauthAccount)) {
            log.info("OAuth2SuccessHandler: 신규 회원 가입 - 이메일: {}", oauthAccount.getEmail());
            member = memberService.createMember(oauthAccount);
            oAuthAccountService.linkMember(oauthAccount, member); // OAuthAccount → Member FK 연결
            boxService.createInitialMyBox(member);
        }else{
            log.info("OAuth2SuccessHandler: 기존 회원 로그인 - 이메일: {}", oauthAccount.getEmail());
            member = memberService.getByOauthAccount(oauthAccount);
        }

        // 토큰 발급 (수명은 JwtProperties 기준 — refresh 회전과 동일 정합)
        String refreshToken = tokenProvider.generateToken(member, jwtProperties.refreshTokenExpiry());
        saveRefreshToken(member, refreshToken, request);
        String accessToken = tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry());

        // 토큰을 URL이 아니라 HttpOnly 쿠키로 직접 전달 (URL 누출 제거).
        // 쿠키 보관기간(maxAge)은 세션 수명 = refresh 만료로 통일(access 쿠키는 컨테이너, 토큰 자체는 JWT exp로 만료).
        addTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME, accessToken, jwtProperties.refreshTokenExpiry());
        addTokenCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, jwtProperties.refreshTokenExpiry());

        // 인증 관련 설정값과 쿠키 제거
        clearAuthenticationAttributes(request, response);

        // 토큰 없이 프론트 콜백 경로로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, REDIRECT_PATH);
    }

    private void saveRefreshToken(Member member, String newRefreshToken, HttpServletRequest request) {
        String ip = extractClientIp(request);
        String deviceInfo = request.getHeader("User-Agent");
        refreshTokenSessionService.save(member, newRefreshToken, ip, deviceInfo);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // 인증 관련 설정값과 쿠키 제거
    // 인증 프로세스를 진행하면서 세션과 쿠키에 임시로 저장해둔 인증 관련 데이터를 제거한다
    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    // 토큰을 HttpOnly 쿠키로 응답에 추가. 도메인/Secure는 환경별(CookieProperties)로 분기.
    private void addTokenCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/")
                .maxAge(maxAge);
        // domain이 비어있으면(로컬) 속성 생략 → host-only 쿠키
        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isBlank()) {
            builder.domain(cookieProperties.getDomain());
        }
        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

}
