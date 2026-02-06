package net.watchbox.domain.box.service.request;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.response.Invitation.InvitationReceivedResponse;
import net.watchbox.domain.box.dto.response.Invitation.InvitationSentResponse;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.request.InviteBoxRequest;
import net.watchbox.domain.box.entity.request.RequestStatus;
import net.watchbox.domain.box.repository.request.InviteBoxRequestRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteBoxRequestService {
    private final InviteBoxRequestRepository inviteBoxRequestRepository;

    public InviteBoxRequest findById(Long requestId) {
        return inviteBoxRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOX_NOT_FOUND, requestId));
    }

    // 공유 박스 보낸초대 엔티티 생성 (PENDING)
    @Transactional
    public void inviteToBox(Member sender, Box box, Member receiver) {
        inviteBoxRequestRepository.save(InviteBoxRequest.builder()
                .sender(sender)
                .box(box)
                .receiver(receiver)
                .status(RequestStatus.PENDING)
                .build());
    }


    // 공유 박스 받은초대 수락 (ACCEPTED)
    @Transactional
    public void acceptBoxInvitation(Member receiver, InviteBoxRequest inviteBoxRequest) {
        validateReceiver(inviteBoxRequest.getReceiver(), receiver);
        inviteBoxRequest.updateStatus(RequestStatus.ACCEPTED);
    }

    // 공유 박스 받은초대 거절 (REJECTED)
    @Transactional
    public void rejectBoxInvitation(Member receiver, InviteBoxRequest inviteBoxRequest) {
        validateReceiver(inviteBoxRequest.getReceiver(), receiver);
        inviteBoxRequest.updateStatus(RequestStatus.REJECTED);
    }

    // 공유 박스 보낸초대 리스트 조회
    public List<InvitationSentResponse> getBoxInvitationsSent(Member member) {
        return inviteBoxRequestRepository.findAllBySender(member).stream()
                .map(InvitationSentResponse::from)
                .toList();
    }

    // 공유 박스 받은초대 리스트 조회
    public List<InvitationReceivedResponse> getBoxInvitationsReceived(Member member) {
        return inviteBoxRequestRepository.findAllByReceiver(member).stream()
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
        if (inviteBoxRequestRepository.existsByBoxAndReceiverAndStatus(box, receiver, RequestStatus.PENDING)) {
            throw new CustomException(ErrorCode.DUPLICATE_INVITE_REQUEST, receiver.getMemberId(), "Member");
        }
    }

    // 이미 처리된 요청인지지 검증 (대기중인 요청 상태인지 검증)
    public void validatePendingInviteRequest(InviteBoxRequest inviteBoxRequest) {
        if (inviteBoxRequest.getStatus() != RequestStatus.PENDING) {
            throw new CustomException(ErrorCode.INVITATION_ALREADY_RESPONDED, inviteBoxRequest.getRequestId());
        }
    }
}
