package net.watchbox.domain.box.facade.invitation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.Invitation.InvitationReceivedResponse;
import net.watchbox.domain.box.dto.Invitation.InvitationSentResponse;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.invitation.BoxInvitation;
import net.watchbox.domain.box.service.member.BoxMemberService;
import net.watchbox.domain.box.service.validation.BoxValidator;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.invitation.BoxInvitationService;
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
    private final BoxInvitationService boxInvitationService;
    private final BoxMemberService boxMemberService;
    private final BoxService boxService;
    private final BoxValidator boxValidator;

    @Transactional
    public InvitationSentResponse inviteToBox(Member sender, Long boxId, Long receiverId) {
        Member receiver = memberService.getByMemberId(receiverId);
        Box box = boxService.getByBoxIdOrElseThrow(boxId);

        // 기존 박스 멤버인지 검증
        boxValidator.validateExistingBoxMember(box, receiver);
        // 초대 요청 중복 검증
        boxValidator.validateDuplicateInviteRequest(box, receiver);
        // 초대 요청 생성
        BoxInvitation boxInvitation = boxInvitationService.inviteToBox(sender, box, receiver);

        return InvitationSentResponse.from(boxInvitation);
    }

    @Transactional (readOnly = true)
    public List<InvitationSentResponse> getBoxInvitationsSent(Member member) {
        List<BoxInvitation> sentInvitations = boxInvitationService.getAllBySender(member);
        return sentInvitations.stream()
                .map(InvitationSentResponse::from)
                .toList();
    }

    @Transactional (readOnly = true)
    public List<InvitationReceivedResponse> getBoxInvitationsReceived(Member member) {
        List<BoxInvitation> receivedInvitations = boxInvitationService.getAllByReceiver(member);
        return receivedInvitations.stream()
                .map(InvitationReceivedResponse::from)
                .toList();
    }

    @Transactional
    public void acceptBoxInvitation(Member member, Long requestId) {
        BoxInvitation boxInvitation = boxInvitationService.findById(requestId);
        Box box = boxInvitation.getBox();

        // 이미 처리된 요청인지지 검증
        boxValidator.validatePendingInviteRequest(boxInvitation);
        // 수신자 본인인지 검증
        boxValidator.validateReceiver(member, boxInvitation.getReceiver());
        // 수락 처리
        boxInvitationService.acceptBoxInvitation(member, boxInvitation);
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
        BoxInvitation boxInvitation = boxInvitationService.findById(requestId);
        // 이미 처리된 요청인지지 검증
        boxValidator.validatePendingInviteRequest(boxInvitation);
        // 수신자 본인인지 검증
        boxValidator.validateReceiver(member, boxInvitation.getReceiver());
        // 거절 처리
        boxInvitationService.rejectBoxInvitation(member, boxInvitation);
    }

    @Transactional
    public void cancelBoxInvitation(Member member, Long requestId) {
        BoxInvitation boxInvitation = boxInvitationService.findById(requestId);
        // 이미 처리된 요청인지지 검증
        boxValidator.validatePendingInviteRequest(boxInvitation);
        // 송신자 본인인지 검증
        boxValidator.validateSender(member, boxInvitation.getSender());
        // 초대 요청 취소 (삭제)
        boxInvitationService.deleteBoxInvitation(boxInvitation);

        log.info("BoxInvitation with requestId {} has been cancelled by sender {}", requestId, member.getMemberId());
    }
}
