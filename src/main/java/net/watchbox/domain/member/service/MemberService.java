package net.watchbox.domain.member.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.entity.OAuthAccount;
import net.watchbox.domain.member.dto.request.ProfileUpdateRequest;
import net.watchbox.domain.member.dto.response.ProfileResponse;
import net.watchbox.domain.member.repository.MemberInvitationProjection;
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
public class MemberService {
    private final MemberRepository memberRepository;

    private static final List<String> ADJECTIVES = List.of(
            "신비한", "용감한", "조용한", "빛나는", "고독한",
            "차가운", "따뜻한", "냉철한", "몽환적", "메소드"
    );

    private static final List<String> NOUNS = List.of(
            "햄릿", "셜록", "조커", "줄리엣", "스칼렛",
            "엘리스", "킹콩", "주디", "알라딘", "토토로"
    );

    private String generateNickname() {
        String adjective = ADJECTIVES.get(RANDOM.nextInt(ADJECTIVES.size()));
        String noun = NOUNS.get(RANDOM.nextInt(NOUNS.size()));
        int number = 100 + RANDOM.nextInt(900);
        String nickname = adjective + noun + number;

        while (memberRepository.existsByNickname(nickname)) {
            number = 100 + RANDOM.nextInt(900);
            nickname = adjective + noun + number;
        }

        return nickname;
    }

    private static final Random RANDOM = new Random();

    public boolean notExistsByOauthAccount(OAuthAccount oauthAccount) {
        return memberRepository.findByOauthAccount(oauthAccount).isEmpty();
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

    public Member getByOauthAccount(OAuthAccount oauthAccount) {
        return memberRepository.findByOauthAccount(oauthAccount)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Member getByMemberIdOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Member getByNicknameOrThrow(String nickname) {
        return memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, nickname));
    }

    public Member getByNameOrThrow(String name) {
        return memberRepository.findByOauthAccount_Name(name);
    }

    public ProfileResponse getProfile(Member member) {
        return ProfileResponse.from(member);
    }

//    public List<Member> searchMemberList(String keyword) {
//        return memberRepository.findByNicknameContainingIgnoreCase(keyword);
//        return MemberSearchPageResponse.builder()
//                .memberList(members.stream().map(MemberResponse::from).toList())
//                .totalCount(members.size())
//                .build();
//    }

    // MemberService - 조회만 (본인은 결과에서 제외)
    public List<MemberInvitationProjection> searchMembersForBoxInvitation(String query, Long boxId, Long excludeMemberId) {
        String formattedQuery = query.trim();
        return memberRepository.findMembersWithInvitationStatus(formattedQuery, boxId, excludeMemberId);
    }

    @Transactional
    public Member updateProfile(Member member, ProfileUpdateRequest request) {
            if (request.getNickname() != null && !request.getNickname().isBlank()) {
                if (existsByNickname(request.getNickname())) {
                    throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
                }
                member.updateNickname(request.getNickname());
            }
            return memberRepository.save(member);
    }

    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Transactional
    public void deleteMember(Member member) {
        memberRepository.delete(member);
    }

}
