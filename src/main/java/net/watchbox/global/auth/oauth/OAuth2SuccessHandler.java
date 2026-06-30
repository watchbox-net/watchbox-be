package net.watchbox.global.auth.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.auth.entity.OAuthAccount;
import net.watchbox.domain.auth.service.NativeAuthService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${url.oauth-callback}")
    private String REDIRECT_PATH;

    private final NativeAuthService nativeAuthService;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        if (!(oAuth2User instanceof CustomOAuth2User customUser)) {
            throw new IllegalArgumentException("OAuth2User is not an instance of CustomOAuth2User");
        }

        OAuthAccount oauthAccount = customUser.getOauthAccount();
        Member member = nativeAuthService.findOrCreateMember(oauthAccount);
        nativeAuthService.issueTokensAndSetCookies(member, request, response);

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, REDIRECT_PATH);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
