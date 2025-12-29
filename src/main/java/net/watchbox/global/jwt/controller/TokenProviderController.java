package net.watchbox.global.jwt.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.global.jwt.dto.AccessTokenCreateRequest;
import net.watchbox.global.jwt.dto.AccessTokenCreateResponse;
import net.watchbox.global.jwt.service.TokenProvider;
import net.watchbox.global.jwt.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/token")
@Tag(name = "TokenProvider", description = "TokenProvider API")
public class TokenProviderController {
    private final TokenService tokenService;
    private final TokenProvider tokenProvider;
    private final MemberService memberService;

    // 리프레시 토큰으로 새로운 액세스 토큰을 발급
    @PostMapping("/access-token")
    public ResponseEntity<AccessTokenCreateResponse> createNewAccessToken(@RequestBody AccessTokenCreateRequest request){
        Long tokenMemberId = tokenProvider.getMemberId(request.getRefreshToken());
        Member member = memberService.findById(tokenMemberId);

        String newAccessToken = tokenService.createNewAccessToken(member, request.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AccessTokenCreateResponse(newAccessToken));
    }
}
