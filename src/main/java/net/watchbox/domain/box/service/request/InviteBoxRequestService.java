package net.watchbox.domain.box.service.request;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.response.InviteBoxRequestResponse;
import net.watchbox.domain.box.entity.SharedBox;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.request.InviteBoxRequest;
import net.watchbox.domain.box.entity.member.BoxMemberRole;
import net.watchbox.domain.box.entity.request.RequestStatus;
import net.watchbox.domain.box.repository.BoxMemberRepository;
import net.watchbox.domain.box.repository.SharedBoxRepository;
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
    private final SharedBoxRepository sharedBoxRepository;
    private final BoxMemberRepository boxMemberRepository;
    private final InviteBoxRequestRepository inviteBoxRequestRepository;

    // 공유 박스 신청 (PENDING)
    @Transactional
    public void requestSharedBox(Member sender, Member receiver) {
        inviteBoxRequestRepository.save(InviteBoxRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(RequestStatus.PENDING)
                .build());
    }

    // ToDo: 공유 박스 신청 알림 전송 (SSE)

    // 공유 박스 신청 리스트 조회
    public List<InviteBoxRequestResponse> getSharedBoxRequests(Member member) {
        return inviteBoxRequestRepository.findByReceiverAndStatus(member, RequestStatus.PENDING).stream()
                .map(InviteBoxRequestResponse::from)
                .toList();
    }

    // 공유 박스 수락 (ACCEPTED)
    @Transactional
    public void acceptSharedBoxRequest(Member receiver, Long requestId) {
        InviteBoxRequest request = inviteBoxRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOX_NOT_FOUND, requestId));
        validateReceiver(request.getReceiver(), receiver);
        request.updateStatus(RequestStatus.ACCEPTED);
        inviteBoxRequestRepository.save(request);

        // 공유 박스 생성
        // ToDo: 각각의 서비스에서 수행하기
        SharedBox sharedBox = sharedBoxRepository.save(SharedBox.builder()
                .build());
        BoxMember boxMemberA = boxMemberRepository.save(BoxMember.builder()
                .sharedBox(sharedBox)
                .member(request.getReceiver())
                .role(BoxMemberRole.OWNER)
                .build());
        BoxMember boxMemberB = boxMemberRepository.save(BoxMember.builder()
                .sharedBox(sharedBox)
                .member(request.getSender())
                .role(BoxMemberRole.OWNER)
                .build());

        log.info("Shared box created with BoxID: {}, Member IDs: {} and {}"
                , sharedBox.getBoxId(), boxMemberA.getMember().getMemberId(), boxMemberB.getMember().getMemberId());
    }

    // 공유 박스 거절 (REJECTED)
    @Transactional
    public void rejectSharedBoxRequest(Member receiver, Long requestId) {
        InviteBoxRequest request = inviteBoxRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOX_NOT_FOUND, requestId));
        validateReceiver(request.getReceiver(), receiver);
        request.updateStatus(RequestStatus.REJECTED);
    }



    // 수신자 검증
    private void validateReceiver(Member actor, Member receiver) {
        if (!actor.equals(receiver)) {
            throw new CustomException(ErrorCode.REQUEST_UNAUTHORIZED_ACCESS);
        }
    }
}
