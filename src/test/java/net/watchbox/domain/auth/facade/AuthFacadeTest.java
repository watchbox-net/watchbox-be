package net.watchbox.domain.auth.facade;

import net.watchbox.domain.auth.dto.SocialUserInfo;
import net.watchbox.domain.auth.entity.OAuthAccount;
import net.watchbox.domain.auth.entity.OAuthProvider;
import net.watchbox.domain.auth.service.AppleNativeAuthService;
import net.watchbox.domain.auth.service.AuthService;
import net.watchbox.domain.auth.service.GoogleNativeAuthService;
import net.watchbox.domain.auth.service.OAuthAccountService;
import net.watchbox.domain.auth.service.OAuthOneTimeCodeService;
import net.watchbox.domain.auth.service.TokenService;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberQueryService;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.dto.response.exception.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 네이티브 소셜 로그인 provider 라우팅 검증.
 * - 구글 → GoogleNativeAuthService, 애플 → AppleNativeAuthService 로 분기
 * - 미지원 provider → UNSUPPORTED_OAUTH_PROVIDER
 * - 신규 회원일 때만 박스 초기화
 */
@ExtendWith(MockitoExtension.class)
class AuthFacadeTest {
    @Mock OAuthAccountService oAuthAccountService;
    @Mock GoogleNativeAuthService googleNativeAuthService;
    @Mock AppleNativeAuthService appleNativeAuthService;
    @Mock AuthService authService;
    @Mock BoxService boxService;
    @InjectMocks AuthFacade authFacade;

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    private OAuthAccount account(OAuthProvider provider, String sub) {
        return OAuthAccount.builder()
                .oauthProvider(provider)
                .oauthId(sub)
                .email("user@example.com")
                .name("user")
                .build();
    }

    @Test
    void 구글_네이티브_로그인은_구글서비스로_분기한다() {
        SocialUserInfo userInfo = new SocialUserInfo("g-sub", "user@example.com", "user");
        OAuthAccount acc = account(OAuthProvider.GOOGLE, "g-sub");
        Member member = org.mockito.Mockito.mock(Member.class);

        given(googleNativeAuthService.exchangeServerAuthCode("server-auth-code")).willReturn(userInfo);
        given(oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.GOOGLE, userInfo)).willReturn(acc);
        given(authService.isNewMemberByOAuthAccount(acc)).willReturn(false);
        given(authService.findOrCreateMemberByOAuthAccount(acc)).willReturn(member);

        authFacade.nativeLogin(OAuthProvider.GOOGLE, "server-auth-code", request, response);

        verify(googleNativeAuthService).exchangeServerAuthCode("server-auth-code");
        verify(authService).issueTokensAndSetCookies(member, request, response);
        verifyNoInteractions(appleNativeAuthService);
    }

    @Test
    void 애플_네이티브_로그인은_애플서비스로_분기한다() {
        SocialUserInfo userInfo = new SocialUserInfo("a-sub", "user@example.com", "user");
        OAuthAccount acc = account(OAuthProvider.APPLE, "a-sub");
        Member member = org.mockito.Mockito.mock(Member.class);

        given(appleNativeAuthService.verifyIdentityToken("identity-token")).willReturn(userInfo);
        given(oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.APPLE, userInfo)).willReturn(acc);
        given(authService.isNewMemberByOAuthAccount(acc)).willReturn(false);
        given(authService.findOrCreateMemberByOAuthAccount(acc)).willReturn(member);

        authFacade.nativeLogin(OAuthProvider.APPLE, "identity-token", request, response);

        verify(appleNativeAuthService).verifyIdentityToken("identity-token");
        verify(authService).issueTokensAndSetCookies(member, request, response);
        verifyNoInteractions(googleNativeAuthService);
    }

    @Test
    void 미지원_provider는_예외를_던진다() {
        assertThatThrownBy(() ->
                authFacade.nativeLogin(OAuthProvider.NAVER, "token", request, response))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("code", "AUTH-400");
    }

    @Test
    void 신규회원이면_박스를_초기화한다() {
        SocialUserInfo userInfo = new SocialUserInfo("g-sub", "user@example.com", "user");
        OAuthAccount acc = account(OAuthProvider.GOOGLE, "g-sub");
        Member member = org.mockito.Mockito.mock(Member.class);

        given(googleNativeAuthService.exchangeServerAuthCode("code")).willReturn(userInfo);
        given(oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.GOOGLE, userInfo)).willReturn(acc);
        given(authService.isNewMemberByOAuthAccount(acc)).willReturn(true);
        given(authService.findOrCreateMemberByOAuthAccount(acc)).willReturn(member);

        authFacade.nativeLogin(OAuthProvider.GOOGLE, "code", request, response);

        verify(boxService).createInitialMyBox(member);
    }

    @Test
    void 기존회원이면_박스를_초기화하지_않는다() {
        SocialUserInfo userInfo = new SocialUserInfo("g-sub", "user@example.com", "user");
        OAuthAccount acc = account(OAuthProvider.GOOGLE, "g-sub");
        Member member = org.mockito.Mockito.mock(Member.class);

        given(googleNativeAuthService.exchangeServerAuthCode("code")).willReturn(userInfo);
        given(oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.GOOGLE, userInfo)).willReturn(acc);
        given(authService.isNewMemberByOAuthAccount(acc)).willReturn(false);
        given(authService.findOrCreateMemberByOAuthAccount(acc)).willReturn(member);

        authFacade.nativeLogin(OAuthProvider.GOOGLE, "code", request, response);

        verify(boxService, never()).createInitialMyBox(org.mockito.ArgumentMatchers.any());
    }
}
