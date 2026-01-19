package net.watchbox.domain.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.service.AuthService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.domain.auth.dto.AuthRefreshRequest;
import net.watchbox.domain.auth.dto.AuthRefreshResponse;
import net.watchbox.global.auth.jwt.TokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Auth API")
public class AuthController {
    private final TokenProvider tokenProvider;
    private final AuthService authService;
    private final MemberService memberService;

    // 리프레시 토큰으로 액세스 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<AuthRefreshResponse> createNewAccessToken(@RequestBody AuthRefreshRequest request){
        Long tokenMemberId = tokenProvider.getMemberId(request.getRefreshToken());
        Member member = memberService.findById(tokenMemberId);

        String newAccessToken = authService.createNewAccessToken(member, request.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthRefreshResponse(newAccessToken));
    }
}
