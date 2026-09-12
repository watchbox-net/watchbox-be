package net.watchbox.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.dto.NativeLoginRequest;
import net.watchbox.domain.auth.facade.AuthFacade;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 소셜 로그인 진입점 통합 컨트롤러 (구글·애플 / 네이티브·웹).
 *
 * <p>성격이 다른 두 부류가 함께 있다:
 * <ul>
 *   <li><b>네이티브</b> — {@code POST /api/auth/login} : 앱이 SDK 로 받은 토큰을 JSON 으로 넘겨 로그인</li>
 *   <li><b>웹(리다이렉트)</b> — {@code /oauth2/{provider}/*} : 브라우저 리다이렉트 방식 진입점</li>
 * </ul>
 * 실제 로직은 {@link AuthFacade} 를 통해 위임한다(네이티브·웹 모두 facade 단일 진입).
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "0-1 [Auth] Social Login")
public class SocialLoginController {
    private final AuthFacade authFacade;

    // ─── 네이티브 (앱 WebView) ─────────────────────────────────────────────

    /**
     * 네이티브 앱(WebView)에서 소셜 SDK로 얻은 토큰으로 로그인.
     * - Google: serverAuthCode (offlineAccess=true 로 획득)
     * - Apple: identityToken(JWT, Apple JWKS 로 검증)
     * provider 는 요청 본문으로 전달한다. 성공 시 기존 redirect OAuth와 동일한 HttpOnly 쿠키를 설정하고 200 반환.
     */
    @PostMapping("/api/auth/login")
    @Operation(summary = "네이티브 소셜 로그인 (구글, 애플 공용)",
            description = "WebView 앱에서 네이티브 SDK 로 얻은 토큰으로 로그인합니다. provider 는 요청 본문으로 전달합니다. "
                    + "(GOOGLE: serverAuthCode, APPLE: identityToken)")
    public ResponseEntity<ApiResponse<Void>> nativeLogin(
            @RequestBody @Valid NativeLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        authFacade.nativeLogin(request.getProvider(), request.getToken(), httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ─── 웹 (브라우저 리다이렉트) ───────────────────────────────────────────

    /** 애플 웹 로그인 진입 → 애플 인증 페이지로 리다이렉트 (state 쿠키 심음). */
    @GetMapping("/oauth2/apple/authorize")
    @Operation(summary = "애플 웹 로그인 진입", description = "웹 애플 로그인 버튼이 이 주소로 이동합니다. state 쿠키를 심고 애플 인증 페이지로 302 리다이렉트합니다.")
    public void appleAuthorize(HttpServletResponse response) throws IOException {
        response.sendRedirect(authFacade.appleWebAuthorizeUrl(response));
    }

    /** 애플이 form_post 로 보내는 콜백. 프론트가 직접 호출하지 않으므로 Swagger 에서 숨김. */
    @PostMapping("/oauth2/apple/callback")
    @Operation(hidden = true, summary = "애플 웹 로그인 콜백")
    public void appleCallback(
            @RequestParam(name = "id_token", required = false) String idToken,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        response.sendRedirect(authFacade.handleAppleWebCallback(idToken, state, error, request, response));
    }

    /** 구글 웹 로그인 진입 → Spring Security 진입 경로로 위임 리다이렉트. */
    @GetMapping("/oauth2/google/authorize")
    @Operation(summary = "구글 웹 로그인 진입", description = "웹 구글 로그인 버튼이 이 주소로 이동합니다. Spring Security 구글 진입 경로로 리다이렉트합니다.")
    public void googleAuthorize(HttpServletResponse response) throws IOException {
        response.sendRedirect(authFacade.googleWebAuthorizeRedirect(response));
    }
}
