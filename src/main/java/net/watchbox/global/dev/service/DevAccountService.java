package net.watchbox.global.dev.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.entity.OAuthAccount;
import net.watchbox.domain.auth.entity.OAuthProvider;
import net.watchbox.domain.auth.repository.OAuthAccountRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DevAccountService {
    private final OAuthAccountRepository oauthAccountRepository;
    private final MemberRepository memberRepository;

    public Member createDevMember() {
        OAuthAccount oauthAccount = oauthAccountRepository.save(OAuthAccount.builder()
                .oauthProvider(OAuthProvider.GOOGLE)
                .oauthId("tester-oauth-id")
                .email("tester0@gmail.com")
                .name("tester0")
                .build());
        Member member = memberRepository.save(Member.builder()
                .email(oauthAccount.getEmail())
                .nickname(oauthAccount.getName())
                .build());
        oauthAccount.linkMember(member); // OAuthAccount → Member FK 연결
        oauthAccountRepository.save(oauthAccount);
        return member;
    }
}
