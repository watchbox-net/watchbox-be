package net.watchbox.global.dev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.auth.service.TokenService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.repository.MemberRepository;
import net.watchbox.global.dev.dto.DevTokenResponse;
import net.watchbox.global.dto.response.ApiResponse;
import net.watchbox.global.dto.response.exception.ErrorDetail;
import net.watchbox.global.properties.AdminProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev")
@Tag(name = "DevAccount")
public class DevAccountController {
    private final AdminProperties adminProperties;
    private final TokenService tokenService;
    private final MemberRepository memberRepository;

    @Operation(summary = "관리자 인증", description = "API Key를 이용한 관리자 인증")
    @PostMapping("/auth")
    public ResponseEntity<ApiResponse<Void>> adminAuth(
            @RequestBody Map<String, String> body
    ) {
        String apiKey = body.get("apiKey");
        if (!apiKey.equals(adminProperties.getApiKey())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(
                            ErrorDetail.builder()
                                    .code("ADMIN-401")
                                    .message("관리자 인증 실패")
                                    .build()
                    ));
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/login/id/{accountId}")
    public DevTokenResponse loginById(
            @PathVariable Long accountId,
            HttpServletRequest request
    ) {
        Member member = memberRepository.findById(accountId).orElseThrow();

        String ip = extractClientIp(request);
        String deviceInfo = request.getHeader("User-Agent");
        String refreshToken = tokenService.issueRefreshToken(member, ip, deviceInfo);
        String accessToken = tokenService.createAccessToken(member);

        return new DevTokenResponse(accessToken, refreshToken, member.getMemberId());
    }

    @GetMapping("/login/nickname/{nickname}")
    public DevTokenResponse loginByNickname(
            @PathVariable String nickname,
            HttpServletRequest request
    ) {
        Member member = memberRepository.findByNickname(nickname).orElseThrow();

        String ip = extractClientIp(request);
        String deviceInfo = request.getHeader("User-Agent");
        String refreshToken = tokenService.issueRefreshToken(member, ip, deviceInfo);
        String accessToken = tokenService.createAccessToken(member);

        return new DevTokenResponse(accessToken, refreshToken, member.getMemberId());
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @GetMapping("/member")
    public String getMember(
            @AuthenticationPrincipal Member member
    ) {
        return "ID: " + member.getMemberId() + "\nName: " + member.getNickname();
    }
}
