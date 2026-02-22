package net.watchbox.domain.box.facade.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.response.Invitation.InvitationReceivedResponse;
import net.watchbox.domain.box.dto.response.Invitation.InvitationSentResponse;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.invitation.BoxInvitation;
import net.watchbox.domain.box.service.BoxMemberService;
import net.watchbox.domain.box.service.BoxValidator;
import net.watchbox.domain.box.service.BoxService;
import net.watchbox.domain.box.service.request.InviteBoxRequestService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BoxInvitationFacade {
    private final MemberService memberService;
    private final InviteBoxRequestService inviteBoxRequestService;
    private final BoxMemberService boxMemberService;
    private final BoxService boxService;
    private final BoxValidator boxValidator;

    @Transactional
    public InvitationSentResponse inviteToBox(Member sender, Long boxId, Long receiverId) {
        Member receiver = memberService.getByMemberId(receiverId);
        Box box = boxService.getByBoxId(boxId);

        // 기존 박스 멤버인지 검증
        boxValidator.validateExistingBoxMember(box, receiver);
        // 초대 요청 중복 검증
        inviteBoxRequestService.validateDuplicateInviteRequest(box, receiver);
        // 초대 요청 생성
        BoxInvitation boxInvitation = inviteBoxRequestService.inviteToBox(sender, box, receiver);

        return InvitationSentResponse.from(boxInvitation);
    }

    @Transactional (readOnly = true)
    public List<InvitationSentResponse> getBoxInvitationsSent(Member member) {
        return inviteBoxRequestService.getBoxInvitationsSent(member);
    }

    @Transactional (readOnly = true)
    public List<InvitationReceivedResponse> getBoxInvitationsReceived(Member member) {
        return inviteBoxRequestService.getBoxInvitationsReceived(member);
    }

    @Transactional
    public void acceptBoxInvitation(Member member, Long requestId) {
        BoxInvitation boxInvitation = inviteBoxRequestService.findById(requestId);
        Box box = boxInvitation.getBox();

        // 이미 처리된 요청인지지 검증
        inviteBoxRequestService.validatePendingInviteRequest(boxInvitation);
        // 수락 처리
        inviteBoxRequestService.acceptBoxInvitation(member, boxInvitation);
        // 박스 멤버(EDITOR 권한)로 추가
        boxMemberService.addEditorToBox(member, box);

        List<BoxMember> boxMembers = boxMemberService.findAllBySharedBox(box);
        List<String> memberNames = boxMembers.stream()
                .map(boxMember -> boxMember.getMember().getNickname())
                .toList();
        box.updateAutoTitle(memberNames);
    }

    @Transactional
    public void rejectBoxInvitation(Member member, Long requestId) {
        BoxInvitation boxInvitation = inviteBoxRequestService.findById(requestId);
        // 이미 처리된 요청인지지 검증
        inviteBoxRequestService.validatePendingInviteRequest(boxInvitation);
        // 거절 처리
        inviteBoxRequestService.rejectBoxInvitation(member, boxInvitation);
    }

}
