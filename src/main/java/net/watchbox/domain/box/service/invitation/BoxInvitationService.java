package net.watchbox.domain.box.service.invitation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.Invitation.InvitationReceivedResponse;
import net.watchbox.domain.box.dto.Invitation.InvitationSentResponse;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.invitation.BoxInvitation;
import net.watchbox.domain.box.entity.invitation.RequestStatus;
import net.watchbox.domain.box.repository.invitation.BoxInvitationRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoxInvitationService {
    private final BoxInvitationRepository boxInvitationRepository;

    public BoxInvitation findById(Long requestId) {
        return boxInvitationRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOX_NOT_FOUND, requestId));
    }

    // 공유 박스 보낸초대 엔티티 생성 (PENDING)
    @Transactional
    public BoxInvitation inviteToBox(Member sender, Box box, Member receiver) {
        return boxInvitationRepository.save(BoxInvitation.builder()
                .sender(sender)
                .box(box)
                .receiver(receiver)
                .status(RequestStatus.PENDING)
                .build());
    }


    // 공유 박스 받은초대 수락 (ACCEPTED)
    @Transactional
    public void acceptBoxInvitation(Member receiver, BoxInvitation boxInvitation) {
        validateReceiver(boxInvitation.getReceiver(), receiver);
        boxInvitation.updateStatus(RequestStatus.ACCEPTED);
    }

    // 공유 박스 받은초대 거절 (REJECTED)
    @Transactional
    public void rejectBoxInvitation(Member receiver, BoxInvitation boxInvitation) {
        validateReceiver(boxInvitation.getReceiver(), receiver);
        boxInvitation.updateStatus(RequestStatus.REJECTED);
    }

    // 공유 박스 보낸초대 리스트 조회
    public List<InvitationSentResponse> getBoxInvitationsSent(Member member) {
        return boxInvitationRepository.findAllBySender(member).stream()
                .map(InvitationSentResponse::from)
                .toList();
    }

    // 공유 박스 받은초대 리스트 조회
    public List<InvitationReceivedResponse> getBoxInvitationsReceived(Member member) {
        return boxInvitationRepository.findAllByReceiver(member).stream()
                .map(InvitationReceivedResponse::from)
                .toList();
    }

    // 수신자 검증
    private void validateReceiver(Member actor, Member receiver) {
        if (!actor.getMemberId().equals(receiver.getMemberId())) {
            throw new CustomException(ErrorCode.REQUEST_UNAUTHORIZED_ACCESS);
        }
    }

    // 중복 초대 요청 검증
    public void validateDuplicateInviteRequest(Box box, Member receiver) {
        if (boxInvitationRepository.existsByBoxAndReceiverAndStatus(box, receiver, RequestStatus.PENDING)) {
            throw new CustomException(ErrorCode.DUPLICATE_INVITE_REQUEST, receiver.getMemberId(), "Member");
        }
    }

    // 이미 처리된 요청인지지 검증 (대기중인 요청 상태인지 검증)
    public void validatePendingInviteRequest(BoxInvitation boxInvitation) {
        if (boxInvitation.getStatus() != RequestStatus.PENDING) {
            throw new CustomException(ErrorCode.INVITATION_ALREADY_RESPONDED, boxInvitation.getRequestId());
        }
    }
}
