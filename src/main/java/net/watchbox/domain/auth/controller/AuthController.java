package net.watchbox.domain.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.auth.dto.CodeExchangeRequest;
import net.watchbox.domain.auth.dto.NativeLoginRequest;
import net.watchbox.domain.auth.dto.TokenRefreshRequest;
import net.watchbox.domain.auth.dto.TokenRefreshResponse;
import net.watchbox.domain.auth.entity.OAuthAccount;
import net.watchbox.domain.auth.entity.OAuthProvider;
import net.watchbox.domain.auth.service.GoogleNativeAuthService;
import net.watchbox.domain.auth.service.NativeAuthService;
import net.watchbox.domain.auth.service.OAuthAccountService;
import net.watchbox.domain.auth.service.OAuthOneTimeCodeService;
import net.watchbox.domain.auth.service.TokenService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.dto.response.ApiResponse;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 API")
@Slf4j
public class AuthController {

    private final TokenProvider tokenProvider;
    private final TokenService tokenService;
    private final MemberService memberService;
    private final NativeAuthService nativeAuthService;
    private final OAuthAccountService oAuthAccountService;
    private final OAuthOneTimeCodeService oAuthOneTimeCodeService;
    private final GoogleNativeAuthService googleNativeAuthService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> createNewAccessToken(@RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!tokenProvider.validToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long tokenMemberId = tokenProvider.getMemberId(refreshToken);
        Member member = memberService.getByMemberIdOrThrow(tokenMemberId);

        TokenRefreshResponse response = tokenService.rotate(member, refreshToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 하이브리드 로그인(로컬 웹 ↔ 개발 서버) 전용 code → 토큰 교환.
     * localhost 는 백엔드 Set-Cookie 를 못 받으므로, 로컬 Next.js 서버가 이 API 로 토큰을
     * 응답 바디로 받아 자기 도메인 쿠키를 직접 심는다. code 는 1회용(30초 TTL).
     */
    @PostMapping("/exchange")
    public ResponseEntity<TokenRefreshResponse> exchange(
            @RequestBody @Valid CodeExchangeRequest request,
            HttpServletRequest httpRequest) {

        Long memberId = oAuthOneTimeCodeService.consume(request.getOneTimeCode());
        Member member = memberService.getByMemberIdOrThrow(memberId);
        return ResponseEntity.ok(nativeAuthService.issueTokens(member, httpRequest));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Member member) {
        tokenService.logout(member.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 네이티브 앱(WebView)에서 소셜 SDK로 얻은 토큰으로 로그인.
     * - Google: serverAuthCode (offlineAccess=true 로 획득)
     * - Apple: authorizationCode (Phase 4)
     * 성공 시 기존 redirect OAuth와 동일한 HttpOnly 쿠키를 설정하고 200 반환.
     */
    @PostMapping("/login/{provider}")
    public ResponseEntity<ApiResponse<Void>> nativeLogin(
            @PathVariable String provider,
            @RequestBody @Valid NativeLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        OAuthAccount oauthAccount = switch (provider.toLowerCase()) {
            case "google" -> {
                GoogleNativeAuthService.GoogleUserInfo userInfo =
                        googleNativeAuthService.exchangeServerAuthCode(request.getToken());
                yield oAuthAccountService.findOrCreateNativeAccount(
                        OAuthProvider.GOOGLE, userInfo.sub(), userInfo.email(), userInfo.name());
            }
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        };

        Member member = nativeAuthService.findOrCreateMember(oauthAccount);
        nativeAuthService.issueTokensAndSetCookies(member, httpRequest, httpResponse);

        return ResponseEntity.ok(ApiResponse.success());
    }
}
