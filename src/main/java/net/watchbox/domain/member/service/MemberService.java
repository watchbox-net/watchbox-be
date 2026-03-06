package net.watchbox.domain.member.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.member.dto.response.ProfileResponse;
import net.watchbox.domain.member.dto.response.search.MemberInvitationProjection;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.repository.MemberRepository;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Member getByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Member getByNickname(String nickname) {
        return memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
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

    // MemberService - 조회만
    public List<MemberInvitationProjection> searchMembersForBoxInvitation(String query, Long boxId) {
        return memberRepository.findMembersWithInvitationStatus(query, boxId);
    }

    // 닉네임 등록, 수정
    // 닉네임 중복 검사
}
