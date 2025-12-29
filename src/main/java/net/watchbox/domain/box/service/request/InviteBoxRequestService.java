package net.watchbox.domain.box.service.request;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.response.InviteBoxRequestResponse;
import net.watchbox.domain.box.entity.SharedBox;
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
    public void requestSharedBox(Member sender, SharedBox sharedBox, Member receiver) {
        inviteBoxRequestRepository.save(InviteBoxRequest.builder()
                .sender(sender)
                .sharedBox(sharedBox)
                .receiver(receiver)
                .status(RequestStatus.PENDING)
                .build());
    }


    // 공유 박스 받은초대 수락 (ACCEPTED)
    @Transactional
    public void acceptBoxInviteRequest(Member receiver, InviteBoxRequest inviteBoxRequest) {
        validateReceiver(inviteBoxRequest.getReceiver(), receiver);
        inviteBoxRequest.updateStatus(RequestStatus.ACCEPTED);
        inviteBoxRequestRepository.save(inviteBoxRequest);
    }

    // 공유 박스 받은초대 거절 (REJECTED)
    @Transactional
    public void rejectBoxInviteRequest(Member receiver, InviteBoxRequest inviteBoxRequest) {
        validateReceiver(inviteBoxRequest.getReceiver(), receiver);
        inviteBoxRequest.updateStatus(RequestStatus.REJECTED);
    }

    // 공유 박스 보낸초대 리스트 조회
    public List<InviteBoxRequestResponse> getInviteBoxRequestsSent(Member member) {
        return inviteBoxRequestRepository.findByReceiverAndStatus(member, RequestStatus.PENDING).stream()
                .map(InviteBoxRequestResponse::from)
                .toList();
    }



    // 수신자 검증
    private void validateReceiver(Member actor, Member receiver) {
        if (!actor.equals(receiver)) {
            throw new CustomException(ErrorCode.REQUEST_UNAUTHORIZED_ACCESS);
        }
    }
}
