package net.watchbox.global.dev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberQueryService;
import net.watchbox.global.auth.jwt.TokenProvider;
import net.watchbox.global.dev.dto.DevTokenResponse;
import net.watchbox.global.util.HttpRequestUtils;
import net.watchbox.global.dev.service.DevAccountService;
import net.watchbox.global.dto.response.ApiResponse;
import net.watchbox.global.dto.response.exception.ErrorDetail;
import net.watchbox.global.properties.AdminProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev")
@Tag(name = "DevAccount")
public class DevAccountController {
    private final AdminProperties adminProperties;
    private final DevAccountService devAccountService;
    private final TokenProvider tokenProvider;
    private final MemberQueryService memberQueryService;

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
        return devAccountService.loginById(accountId, HttpRequestUtils.extractClientIp(request), request.getHeader("User-Agent"));
    }

    @GetMapping("/login/nickname/{nickname}")
    public DevTokenResponse loginByNickname(
            @PathVariable String nickname,
            HttpServletRequest request
    ) {
        return devAccountService.loginByNickname(nickname, HttpRequestUtils.extractClientIp(request), request.getHeader("User-Agent"));
    }

    @GetMapping("/member")
    public String getMember(@RequestParam String accessToken) {
        Long memberId = tokenProvider.getMemberId(accessToken);
        Member member = memberQueryService.getByMemberIdOrThrow(memberId);
        return "ID: " + member.getMemberId() + "\nName: " + member.getNickname() + "\nEmail: " + member.getEmail();
    }

}
