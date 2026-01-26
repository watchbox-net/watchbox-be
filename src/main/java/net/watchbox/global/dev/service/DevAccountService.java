package net.watchbox.global.dev.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.entity.OauthAccount;
import net.watchbox.domain.auth.entity.OauthProvider;
import net.watchbox.domain.auth.repository.OauthAccountRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DevAccountService {
    private final OauthAccountRepository oauthAccountRepository;
    private final MemberRepository memberRepository;

    public Member createDevMember() {
        OauthAccount oauthAccount = OauthAccount.builder()
                .oauthProvider(OauthProvider.GOOGLE)
                .oauthId("tester-oauth-id")
                .email("tester0@gmail.com")
                .name("tester0")
                .build();
        Member member = Member.builder()
                .oauthAccount(oauthAccount)
                .email(oauthAccount.getEmail())
                .nickname(oauthAccount.getName())
                .build();
        return memberRepository.save(member);
    }
}
