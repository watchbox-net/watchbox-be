package net.watchbox.domain.member.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.invitation.BoxInvitationService;
import net.watchbox.domain.member.dto.response.search.BoxInviteStatus;
import net.watchbox.domain.member.dto.response.search.MemberSearchPageResponse;
import net.watchbox.domain.member.dto.response.search.MemberSearchResponse;
import net.watchbox.domain.member.service.MemberService;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class MemberFacade {
    private final MemberService memberService;
    private final BoxService boxService;
    private final BoxInvitationService boxInvitationService;

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
}
