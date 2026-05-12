package net.watchbox.domain.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.service.TokenService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.domain.auth.dto.TokenRefreshRequest;
import net.watchbox.domain.auth.dto.TokenRefreshResponse;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 API")
public class AuthController {
    private final TokenProvider tokenProvider;
    private final TokenService tokenService;
    private final MemberService memberService;

    // 리프레시 토큰으로 액세스 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> createNewAccessToken(@RequestBody TokenRefreshRequest request){
        Long tokenMemberId = tokenProvider.getMemberId(request.getRefreshToken());
        Member member = memberService.getByMemberIdOrThrow(tokenMemberId);

        String newAccessToken = tokenService.createNewAccessToken(member, request.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TokenRefreshResponse(newAccessToken));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Member member) {
        tokenService.logout(member.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
