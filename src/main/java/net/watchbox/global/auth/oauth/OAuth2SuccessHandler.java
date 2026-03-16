package net.watchbox.global.auth.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.entity.OauthAccount;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.auth.entity.RefreshToken;
import net.watchbox.domain.auth.repository.RefreshTokenRepository;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.util.CookieUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler { // 인증 성공시 실행할 핸들러
    @Value("${url.frontend}")
    private String REDIRECT_PATH;
//    public static final String REDIRECT_PATH = "http://localhost:3000/login/success";

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;

    private final MemberService memberService;
    private final BoxService boxService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // OAuth2UserCustomService의 loadUser()에서 반환한 OAuth2User 사용
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // CustomOAuth2User로 캐스팅
        if (!(oAuth2User instanceof CustomOAuth2User)) {
            throw new IllegalArgumentException("OAuth2User is not an instance of CustomOAuth2User");
        }
        CustomOAuth2User customUser = (CustomOAuth2User) oAuth2User;
        OauthAccount oauthAccount = customUser.getOauthAccount();

        /**
         * 최초 가입시
         * 1. Member 생성
         * 2. MyBox 생성
         */
        Member member;
        if(memberService.notExistsByOauthAccount(oauthAccount)) {
            member = memberService.getByOauthAccount(oauthAccount);
            boxService.createInitialMyBox(member);
        }else{
            member = memberService.createMember(oauthAccount);
        }

        // 리프레시 토큰 생성 -> DB에 저장 -> 쿠키에 저장
        String refreshToken = tokenProvider.generateToken(member, REFRESH_TOKEN_DURATION);
        saveRefreshToken(member.getMemberId(), refreshToken);
        addRefreshTokenToCookie(request, response, refreshToken); // 쿠키에 토큰 저장 제외

        // 액세스 토큰 생성 -> 패스에 엑세스 토큰 추가
        String accessToken = tokenProvider.generateToken(member, ACCESS_TOKEN_DURATION);
        String targetUrl = getTargetUrl(accessToken, refreshToken); // 액세스, 리프레시 모두 전달

        // 인증 관련 설정값과 쿠키 제거
        clearAuthenticationAttributes(request, response);

        // 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    // 생성된 리프레시 토큰을 전달받아 유저 아이디와 데이터베이스에 저장
    private void saveRefreshToken(Long memberId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByMemberId(memberId)
                .map(entity -> entity.update(newRefreshToken))
                .orElse(RefreshToken.builder()
                        .memberId(memberId)
                        .refreshToken(newRefreshToken)
                        .build());
        refreshTokenRepository.save(refreshToken);
    }

    // 액세스 토큰을 패스에 추가
    // 쿠키에서 리다이렉트 경로가 담긴 값을 가져와 쿼리 파라미터에 액세스 토큰을 추가한다
    // 액세스 토큰을 클라이언트에게 전달: http://localhost:8080/artilcles?token=aksdjgl3i.saelkgjald..
    private String getTargetUrl(String accessToken, String refreshToken) {
        return UriComponentsBuilder.fromUriString(REDIRECT_PATH)
                .queryParam("access_token", accessToken)
                .queryParam("refresh_token", refreshToken)
                .build()
                .toUriString();
    }

    // 인증 관련 설정값과 쿠키 제거
    // 인증 프로세스를 진행하면서 세션과 쿠키에 임시로 저장해둔 인증 관련 데이터를 제거한다
    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    // 생성된 리프레시 토큰을 쿠키에 저장.
    // 클라이언트에서 액세스 토큰이 만료되면 재발급 요청하도록 해당 메서드로 쿠키에 리프레시 토큰을 저장
    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }

}
