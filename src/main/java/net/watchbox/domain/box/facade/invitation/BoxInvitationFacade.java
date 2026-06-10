package net.watchbox.domain.box.facade.invitation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.Invitation.InvitationReceivedResponse;
import net.watchbox.domain.box.dto.Invitation.InvitationSentResponse;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.invitation.BoxInvitation;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.box.service.member.BoxMemberService;
import net.watchbox.domain.box.service.validation.BoxValidator;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.invitation.BoxInvitationService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.domain.notification.dto.payload.BoxInvitationPayload;
import net.watchbox.domain.notification.dto.payload.BoxInvitationRespondedPayload;
import net.watchbox.domain.notification.event.BoxInvitationReceivedEvent;
import net.watchbox.domain.notification.event.BoxInvitationRespondedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BoxInvitationFacade {
    private final MemberService memberService;
    private final BoxInvitationService boxInvitationService;
    private final BoxMemberService boxMemberService;
    private final BoxService boxService;
    private final BoxContentQueryService boxContentQueryService;
    private final BoxValidator boxValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public InvitationSentResponse inviteToBox(Member sender, Long boxId, Long receiverId) {
        Member receiver = memberService.getByMemberIdOrThrow(receiverId);
        Box box = boxService.getByBoxIdOrElseThrow(boxId);

        // 기존 박스 멤버인지 검증
        boxValidator.validateExistingBoxMember(box, receiver);
        // 초대 요청 중복 검증
        boxValidator.validateDuplicateInviteRequest(box, receiver);
        // 초대 요청 생성
        BoxInvitation boxInvitation = boxInvitationService.inviteToBox(sender, box, receiver);

        // 알림 도메인 이벤트 발행
        // - AFTER_COMMIT 리스너가 비동기로 Notification 저장 + SSE push 처리
        // - 알림 실패가 초대 트랜잭션에 영향 X
        eventPublisher.publishEvent(new BoxInvitationReceivedEvent(
                receiverId,
                BoxInvitationPayload.of(boxInvitation, box, sender)
        ));

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
    public boolean hasReceivedInvitation(Member member) {
        return boxInvitationService.hasPendingReceivedInvitation(member);
    }

    @Transactional (readOnly = true)
    public List<InvitationReceivedResponse> getBoxInvitationsReceived(Member member) {
        List<BoxInvitation> receivedInvitations = boxInvitationService.getAllByReceiverWithBoxAndMembers(member);
        List<Box> boxes = receivedInvitations.stream()
                .map(BoxInvitation::getBox)
                .toList();
        Map<Long, List<String>> posterMap = boxContentQueryService.getRecentPosterPathsByBoxes(boxes);

        return receivedInvitations.stream()
                .map(boxInvitation -> InvitationReceivedResponse.of(boxInvitation,
                        posterMap.getOrDefault(boxInvitation.getBox().getBoxId(), Collections.emptyList())))
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
        boxInvitationService.acceptBoxInvitation(boxInvitation);
        // 박스 멤버(EDITOR 권한)로 추가
        boxMemberService.addEditorToBox(member, box);

        // 결과 알림 이벤트 발행 (수신자 = 원래 sender)
        eventPublisher.publishEvent(new BoxInvitationRespondedEvent(
                boxInvitation.getSender().getMemberId(),
                BoxInvitationRespondedPayload.of(boxInvitation, box, member)
        ));
    }

    @Transactional
    public void rejectBoxInvitation(Member member, Long requestId) {
        BoxInvitation boxInvitation = boxInvitationService.findById(requestId);
        // 이미 처리된 요청인지지 검증
        boxValidator.validatePendingInviteRequest(boxInvitation);
        // 수신자 본인인지 검증
        boxValidator.validateReceiver(member, boxInvitation.getReceiver());
        // 거절 처리
        boxInvitationService.rejectBoxInvitation(boxInvitation);

        // 결과 알림 이벤트 발행 (수신자 = 원래 sender)
        eventPublisher.publishEvent(new BoxInvitationRespondedEvent(
                boxInvitation.getSender().getMemberId(),
                BoxInvitationRespondedPayload.of(boxInvitation, boxInvitation.getBox(), member)
        ));
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

        log.info("Pending boxInvitation requestId {} has been cancelled by sender {}", requestId, member.getMemberId());
    }

    @Transactional
    public void deleteBoxInvitation(Member member, Long requestId) {
        BoxInvitation boxInvitation = boxInvitationService.findById(requestId);
        // 송신자 본인인지 검증
        boxValidator.validateSender(member, boxInvitation.getSender());
        // 초대 요청 취소 (삭제)
        boxInvitationService.deleteBoxInvitation(boxInvitation);

        log.info("Rejected boxInvitation requestId {} has been deleted by sender {}", requestId, member.getMemberId());
    }
}
