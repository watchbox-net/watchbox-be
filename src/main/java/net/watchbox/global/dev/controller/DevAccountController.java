package net.watchbox.global.dev.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.auth.repository.OauthAccountRepository;
import net.watchbox.domain.auth.service.TokenService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.repository.MemberRepository;
import net.watchbox.global.dev.dto.DevTokenResponse;
import net.watchbox.global.properties.JwtProperties;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dev")
@Slf4j
@Tag(name = "DevAccount")
public class DevAccountController {
    private final JwtProperties jwtProperties;
    private final TokenService tokenService;
    private final OauthAccountRepository oauthAccountRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/login/{accountId}")
    public DevTokenResponse login(
            @PathVariable Long accountId
    ) {
        Member member = memberRepository.findById(accountId).orElseThrow();

        String refreshToken = tokenService.createNewRefreshToken(member);
        String accessToken = tokenService.createNewAccessToken(member, refreshToken);

        return new DevTokenResponse(accessToken, refreshToken, member.getMemberId());
    }

    @GetMapping("/member")
    public String getMember(
            @AuthenticationPrincipal Member member
    ) {
        return "ID: " + member.getMemberId() + "\nName: " + member.getNickname();
    }
}
