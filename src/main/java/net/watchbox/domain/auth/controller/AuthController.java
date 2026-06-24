package net.watchbox.domain.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.auth.service.TokenService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.domain.auth.dto.TokenRefreshRequest;
import net.watchbox.domain.auth.dto.TokenRefreshResponse;
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

    // 리프레시 토큰으로 액세스 토큰 갱신 (회전: 새 리프레시 토큰도 함께 발급)
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> createNewAccessToken(@RequestBody TokenRefreshRequest request){
        String refreshToken = request.getRefreshToken();

        // 만료/서명오류면 파싱 전에 401로 거른다.
        // (getMemberId가 만료 토큰을 파싱하면 ExpiredJwtException → 500으로 떨어지던 문제 방지)
        if (!tokenProvider.validToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long tokenMemberId = tokenProvider.getMemberId(refreshToken);
        Member member = memberService.getByMemberIdOrThrow(tokenMemberId);

        TokenRefreshResponse response = tokenService.rotate(member, refreshToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Member member) {
        tokenService.logout(member.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
