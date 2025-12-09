package net.watchbox.domain.box.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.response.CreateBoxRequestResponse;
import net.watchbox.domain.box.entity.SharedBox;
import net.watchbox.domain.box.entity.BoxMember;
import net.watchbox.domain.box.entity.request.CreateBoxRequest;
import net.watchbox.domain.box.enums.BoxMemberRole;
import net.watchbox.domain.box.enums.ShareType;
import net.watchbox.domain.box.enums.RequestStatus;
import net.watchbox.domain.box.repository.BoxMemberRepository;
import net.watchbox.domain.box.repository.SharedBoxRepository;
import net.watchbox.domain.box.repository.request.CreateBoxRequestRepository;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateBoxRequestService {
    private final SharedBoxRepository sharedBoxRepository;
    private final BoxMemberRepository boxMemberRepository;
    private final CreateBoxRequestRepository createBoxRequestRepository;

    // 공유 박스 신청 (PENDING)
    @Transactional
    public void requestSharedBox(Member sender, Member receiver) {
        createBoxRequestRepository.save(CreateBoxRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(RequestStatus.PENDING)
                .build());
    }

    // ToDo: 공유 박스 신청 알림 전송 (SSE)

    // 공유 박스 신청 리스트 조회
    public List<CreateBoxRequestResponse> getSharedBoxRequests(Member member) {
        return createBoxRequestRepository.findByReceiverAndStatus(member, RequestStatus.PENDING).stream()
                .map(CreateBoxRequestResponse::from)
                .toList();
    }

    // 공유 박스 수락 (ACCEPTED)
    @Transactional
    public void acceptSharedBoxRequest(Long requestId) {
        CreateBoxRequest request = createBoxRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOX_NOT_FOUND, requestId));
        request.updateStatus(RequestStatus.ACCEPTED);
        createBoxRequestRepository.save(request);

        // 공유 박스 생성
        SharedBox sharedBox = sharedBoxRepository.save(SharedBox.builder()
                .shareType(ShareType.PRIVATE)
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
    public void rejectSharedBoxRequest(Long requestId) {
        CreateBoxRequest request = createBoxRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOX_NOT_FOUND, requestId));
        request.updateStatus(RequestStatus.REJECTED);
    }




}
