package net.watchbox.domain.member.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.auth.entity.OAuthAccount;
import net.watchbox.domain.member.dto.response.ProfileResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.repository.MemberInvitationProjection;
import net.watchbox.domain.member.repository.MemberRepository;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberQueryService {
    private final MemberRepository memberRepository;

    public boolean notExistsByOauthAccount(OAuthAccount oauthAccount) {
        return memberRepository.findByOauthAccount(oauthAccount).isEmpty();
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

    // 조회만 (본인은 결과에서 제외)
    public List<MemberInvitationProjection> searchMembersForBoxInvitation(String query, Long boxId, Long excludeMemberId) {
        String formattedQuery = query.trim();
        return memberRepository.findMembersWithInvitationStatus(formattedQuery, boxId, excludeMemberId);
    }

    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }
}
