package net.watchbox.domain.member.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.entity.OAuthAccount;
import net.watchbox.domain.member.dto.request.ProfileUpdateRequest;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.repository.MemberRepository;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MemberCommandService {
    private final MemberRepository memberRepository;
    private final MemberQueryService memberQueryService;

    private static final List<String> ADJECTIVES = List.of(
            "신비한", "용감한", "조용한", "빛나는", "고독한",
            "차가운", "따뜻한", "냉철한", "몽환적", "메소드"
    );

    private static final List<String> NOUNS = List.of(
            "햄릿", "셜록", "조커", "줄리엣", "스칼렛",
            "엘리스", "킹콩", "주디", "알라딘", "토토로"
    );

    private static final Random RANDOM = new Random();

    private String generateNickname() {
        String adjective = ADJECTIVES.get(RANDOM.nextInt(ADJECTIVES.size()));
        String noun = NOUNS.get(RANDOM.nextInt(NOUNS.size()));
        int number = 100 + RANDOM.nextInt(900);
        String nickname = adjective + noun + number;

        while (memberQueryService.existsByNickname(nickname)) {
            number = 100 + RANDOM.nextInt(900);
            nickname = adjective + noun + number;
        }

        return nickname;
    }

    // Member 만 생성·저장. OAuthAccount 와의 연결(FK)은 OAuthAccountService.linkMember 로 별도 처리.
    public Member createMember(OAuthAccount oauthAccount) {
        String nickname = generateNickname();
        return memberRepository.save(Member.builder()
                .email(oauthAccount.getEmail())
                .nickname(nickname)
                .build());
    }

    public Member createSampleMember(OAuthAccount oauthAccount, String nickname) {
        return memberRepository.save(Member.builder()
                .email(oauthAccount.getEmail())
                .nickname(nickname)
                .build());
    }

    @Transactional
    public Member updateProfile(Member member, ProfileUpdateRequest request) {
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            if (memberQueryService.existsByNickname(request.getNickname())) {
                throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
            member.updateNickname(request.getNickname());
        }
        return memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(Member member) {
        memberRepository.delete(member);
    }
}
