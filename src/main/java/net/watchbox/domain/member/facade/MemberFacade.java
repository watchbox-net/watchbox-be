package net.watchbox.domain.member.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.auth.service.OAuthAccountService;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.member.BoxMemberRole;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.box.service.member.BoxMemberService;
import net.watchbox.domain.auth.service.TokenService;
import net.watchbox.domain.member.dto.request.ProfileUpdateRequest;
import net.watchbox.domain.member.dto.response.MemberStatsResponse;
import net.watchbox.domain.member.dto.response.MyPageResponse;
import net.watchbox.domain.member.dto.response.ProfileResponse;
import net.watchbox.domain.member.dto.response.search.BoxInviteStatus;
import net.watchbox.domain.member.dto.response.search.MemberSearchPageResponse;
import net.watchbox.domain.member.dto.response.search.MemberSearchResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberCommandService;
import net.watchbox.domain.member.service.MemberQueryService;
import net.watchbox.domain.record.service.record.ContentRecordQueryService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class MemberFacade {
    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;
    private final BoxMemberService boxMemberService;
    private final ContentRecordQueryService contentRecordQueryService;
    private final BoxService boxService;
    private final BoxContentCommandService boxContentCommandService;
    private final TokenService tokenService;
    private final OAuthAccountService oAuthAccountService;

    @Transactional(readOnly = true)
    public MemberSearchPageResponse searchMemberListWithSharedStatus(Member member, String keyword, Long boxId) {
        List<MemberSearchResponse> memberSearchResponseList  = memberQueryService.searchMembersForBoxInvitation(keyword, boxId, member.getMemberId())
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
        ProfileResponse profileResponse = memberQueryService.getProfile(member);
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
        return ProfileResponse.from(memberCommandService.updateProfile(member, request));
    }

    @Transactional
    public void deleteMember(Member member) {
        /* Member 삭제시 cascade 설정으로 자동 삭제
        ContentRecord — 시청 기록 (member.contentRecords, REMOVE)
        BoxMember — 박스 멤버십 (member.boxMembers, ALL) — 본인이 owner인 박스의 BoxMember도 Box→BoxMember cascade로 연쇄 삭제
        BoxInvitation (보낸/받은) — sender/receiver, REMOVE
        BoxJoinRequest (보낸) — sender, REMOVE
        */

        // 모든 BoxContent 삭제
        boxContentCommandService.deleteAllBoxContentByMember(member);

        // MyBox 모두 삭제
        boxService.deleteAllMyBoxes(member);

        // 2인 이상의 SharedBox에서 Owner일 경우 Owner 권한 넘겨주기 (가장 오래된 Editor 권한의 BoxMember)
        List<Box> sharedBoxes = boxService.getAllSharedBoxListByOwner(member);
        for (Box box : sharedBoxes) {
            boxMemberService.findOldestEditorExcludingMember(box, member)
                    .ifPresent(newOwner -> {
                        box.changeOwner(newOwner.getMember());
                        newOwner.changeRole(BoxMemberRole.OWNER);
                    });
        }

        // RefreshToken 삭제
        tokenService.logout(member.getMemberId());

        // OAuthAccount 삭제 — FK(oauth_account.member_id) 자식이므로 Member 보다 먼저 제거
        oAuthAccountService.deleteByMember(member);

        // Member 및 연관된 엔티티들 삭제 (cascade)
        memberCommandService.deleteMember(member);

        // ToDO: 이메일로 탈퇴 회원 정보 보내기
    }
}
