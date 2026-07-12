package net.watchbox.domain.auth.facade;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.dto.SocialUserInfo;
import net.watchbox.domain.auth.dto.TokenRefreshResponse;
import net.watchbox.domain.auth.entity.OAuthAccount;
import net.watchbox.domain.auth.entity.OAuthProvider;
import net.watchbox.domain.auth.service.*;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberQueryService;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {
    private final OAuthAccountService oAuthAccountService;
    private final OAuthOneTimeCodeService oAuthOneTimeCodeService;
    private final GoogleNativeAuthService googleNativeAuthService;
    private final AppleNativeAuthService appleNativeAuthService;
    private final MemberQueryService memberQueryService;
    private final AuthService authService;
    private final TokenProvider tokenProvider;
    private final TokenService tokenService;
    private final BoxService boxService;

    /** refreshToken 검증 후 토큰 회전. */
    public TokenRefreshResponse refresh(String refreshToken) {
        if (!tokenProvider.validToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long tokenMemberId = tokenProvider.getMemberId(refreshToken);
        Member member = memberQueryService.getByMemberIdOrThrow(tokenMemberId);

        return tokenService.rotate(member, refreshToken);
    }

    /**
     * 하이브리드 로그인(로컬 웹 ↔ 개발 서버) 전용 code → 토큰 교환.
     * localhost 는 백엔드 Set-Cookie 를 못 받으므로, 로컬 Next.js 서버가 이 API 로 토큰을
     * 응답 바디로 받아 자기 도메인 쿠키를 직접 심는다. code 는 1회용(30초 TTL).
     */
    public TokenRefreshResponse exchange(String oneTimeCode, HttpServletRequest httpRequest) {
        Long memberId = oAuthOneTimeCodeService.consume(oneTimeCode);
        Member member = memberQueryService.getByMemberIdOrThrow(memberId);
        return authService.issueTokens(member, httpRequest);
    }

    public void logout(Long memberId) {
        tokenService.logout(memberId);
    }

    /**
     * 네이티브 앱(WebView)에서 소셜 SDK로 얻은 토큰으로 로그인.
     * - Google: serverAuthCode (offlineAccess=true 로 획득)
     * - Apple: identityToken(JWT, Apple JWKS 로 검증)
     * 성공 시 기존 redirect OAuth와 동일한 HttpOnly 쿠키를 설정한다.
     */
    public void nativeLogin(OAuthProvider provider, String token,
                             HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        OAuthAccount oauthAccount = switch (provider) {
            case GOOGLE -> {
                SocialUserInfo userInfo = googleNativeAuthService.exchangeServerAuthCode(token);
                yield oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.GOOGLE, userInfo);
            }
            case APPLE -> {
                SocialUserInfo userInfo = appleNativeAuthService.verifyIdentityToken(token);
                yield oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.APPLE, userInfo);
            }
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        };

        boolean isNewMemberByOAuthAccount = authService.isNewMemberByOAuthAccount(oauthAccount);
        Member member = authService.findOrCreateMemberByOAuthAccount(oauthAccount);
        if (isNewMemberByOAuthAccount) {
            boxService.createInitialMyBox(member);
        }
        authService.issueTokensAndSetCookies(member, httpRequest, httpResponse);
    }
}
