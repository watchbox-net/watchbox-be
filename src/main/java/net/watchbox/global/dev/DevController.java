package net.watchbox.global.dev;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.account.repository.OauthAccountRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.repository.MemberRepository;
import net.watchbox.global.jwt.service.TokenService;
import net.watchbox.global.properties.JwtProperties;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dev")
@Slf4j
public class DevController {
    private final JwtProperties jwtProperties;
    private final TokenService tokenService;
    private final OauthAccountRepository oauthAccountRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/login")
    public TokenResponse login() {
        Member member = memberRepository.findById(0L).orElseThrow();
        String refreshToken = tokenService.createNewRefreshToken(member);
        String accessToken = tokenService.createNewAccessToken(member, refreshToken);

        return new TokenResponse(accessToken, refreshToken, member.getMemberId());
    }

    @GetMapping("/member")
    public String getMember(
            @AuthenticationPrincipal Member member
    ) {
        return "ID: " + member.getMemberId() + "\nName: " + member.getNickname();
    }
}
