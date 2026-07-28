package net.watchbox.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.auth.dto.TokenRefreshRequest;
import net.watchbox.domain.auth.dto.TokenRefreshResponse;
import net.watchbox.domain.auth.facade.AuthFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
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
    private final AuthFacade authFacade;

    @PostMapping("/refresh")
    @Operation(summary = "accessToken 재발급", description = "refreshToken 검증 후 새 accessToken 발급")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> createNewAccessToken(@RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authFacade.refresh(request.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃", description = "Redis 에 저장된 리프레시 토큰 세션을 삭제해 무효화합니다.")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Member member) {
        authFacade.logout(member.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
