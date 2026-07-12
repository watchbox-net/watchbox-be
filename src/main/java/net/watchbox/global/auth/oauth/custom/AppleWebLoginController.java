package net.watchbox.global.auth.oauth.custom;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 애플 웹 로그인(리다이렉트) 엔드포인트. 실제 로직은 {@link AppleWebLoginService} 가 담당한다.
 *
 * <p>Spring Security oauth2Login 은 static client-secret 을 요구하지만 Apple 은 client_secret 이
 * ES256 서명 JWT(만료 있음)라 맞지 않아 커스텀으로 처리한다. Spring Security 의
 * {@code /oauth2/authorization/*}, {@code /login/oauth2/code/*} 기본 매처와 겹치지 않도록
 * {@code /oauth2/apple/*} 경로를 쓴다.
 */
@RequiredArgsConstructor
@RestController
public class AppleWebLoginController {
    private final AppleWebLoginService appleWebLoginService;

    /** 애플 인증 페이지로 리다이렉트. */
    @GetMapping("/oauth2/apple/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        response.sendRedirect(appleWebLoginService.createAuthorizeUrl(response));
    }

    /** 애플이 form_post 로 보내는 콜백. 검증·회원처리 후 프론트로 리다이렉트. */
    @PostMapping("/oauth2/apple/callback")
    public void callback(
            @RequestParam(name = "id_token", required = false) String idToken,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        response.sendRedirect(appleWebLoginService.handleCallback(idToken, state, error, request, response));
    }
}
