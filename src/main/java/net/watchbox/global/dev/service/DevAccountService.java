package net.watchbox.global.dev.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.service.TokenService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.repository.MemberRepository;
import net.watchbox.global.dev.dto.DevTokenResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DevAccountService {
    private final MemberRepository memberRepository;
    private final TokenService tokenService;

    public DevTokenResponse login(Member member, String ip, String deviceInfo) {
        String refreshToken = tokenService.issueRefreshToken(member, ip, deviceInfo);
        String accessToken = tokenService.createAccessToken(member);
        return new DevTokenResponse(accessToken, refreshToken, member.getMemberId());
    }

    public DevTokenResponse loginById(Long accountId, String ip, String deviceInfo) {
        Member member = memberRepository.findById(accountId).orElseThrow();
        return login(member, ip, deviceInfo);
    }

    public DevTokenResponse loginByNickname(String nickname, String ip, String deviceInfo) {
        Member member = memberRepository.findByNickname(nickname).orElseThrow();
        return login(member, ip, deviceInfo);
    }
}
