package net.watchbox.domain.member.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.invitation.BoxInvitationService;
import net.watchbox.domain.box.service.member.BoxMemberService;
import net.watchbox.domain.member.dto.request.ProfileUpdateRequest;
import net.watchbox.domain.member.dto.response.MemberStatsResponse;
import net.watchbox.domain.member.dto.response.MyPageResponse;
import net.watchbox.domain.member.dto.response.ProfileResponse;
import net.watchbox.domain.member.dto.response.search.BoxInviteStatus;
import net.watchbox.domain.member.dto.response.search.MemberSearchPageResponse;
import net.watchbox.domain.member.dto.response.search.MemberSearchResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.domain.record.service.ContentRecordQueryService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class MemberFacade {
    private final MemberService memberService;
    private final BoxMemberService boxMemberService;
    private final ContentRecordQueryService contentRecordQueryService;
    private final BoxService boxService;
    private final BoxInvitationService boxInvitationService;

    @Transactional(readOnly = true)
    public MemberSearchPageResponse searchMemberListWithSharedStatus(String keyword, Long boxId) {
        List<MemberSearchResponse> memberSearchResponseList  = memberService.searchMembersForBoxInvitation(keyword, boxId)
                .stream()
                .map(p -> MemberSearchResponse.builder()
                        .memberId(p.getMemberId())
                        .nickname(p.getNickname())
                        .profileImage(p.getProfileImage())
                        .boxInviteStatus(BoxInviteStatus.from(p.getStatus()))
                        .build())
                .toList();
        return MemberSearchPageResponse.builder()
                .memberSearchList(memberSearchResponseList)
                .totalCount(memberSearchResponseList.size())
                .build();
    }

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(Member member) {
        ProfileResponse profileResponse = memberService.getProfile(member);
        long likeCount = contentRecordQueryService.countLikedContentsByMember(member);
        long watchStatusCount = contentRecordQueryService.countWatchStatusByMember(member);
        long boxCount = boxMemberService.countByMember(member); // 회원이 속한 박스 개수 (마이 박스 + 공유 박스) | 박스 멤버 개수가 곧 박스 개수이다
        MemberStatsResponse statsResponse = MemberStatsResponse.builder()
                .likeCount(likeCount)
                .watchStatusCount(watchStatusCount)
                .boxCount(boxCount)
                .build();
        return MyPageResponse.of(profileResponse, statsResponse);
    }

    @Transactional
    public ProfileResponse updateProfile(Member member, ProfileUpdateRequest request) {
        return ProfileResponse.from(memberService.updateProfile(member, request));
    }
}
