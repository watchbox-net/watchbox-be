package net.watchpeople.domain.box.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchpeople.domain.box.dto.response.BoxShareRequestResponse;
import net.watchpeople.domain.box.entity.Box;
import net.watchpeople.domain.box.entity.BoxMember;
import net.watchpeople.domain.box.entity.SharedBoxRequest;
import net.watchpeople.domain.box.enums.BoxMemberRole;
import net.watchpeople.domain.box.enums.BoxType;
import net.watchpeople.domain.box.enums.RequestStatus;
import net.watchpeople.domain.box.repository.BoxMemberRepository;
import net.watchpeople.domain.box.repository.BoxRepository;
import net.watchpeople.domain.box.repository.SharedBoxRequestRepository;
import net.watchpeople.domain.member.entity.Member;
import net.watchpeople.global.dto.response.exception.CustomException;
import net.watchpeople.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedBoxRequestService {
    private final BoxRepository boxRepository;
    private final BoxMemberRepository boxMemberRepository;
    private final SharedBoxRequestRepository sharedBoxRequestRepository;

    // 공유 박스 신청 (PENDING)
    @Transactional
    public void requestSharedBox(Member sender, Member receiver) {
        sharedBoxRequestRepository.save(SharedBoxRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(RequestStatus.PENDING)
                .build());
    }

    // ToDo: 공유 박스 신청 알림 전송 (SSE)

    // 공유 박스 신청 리스트 조회
    public List<BoxShareRequestResponse> getSharedBoxRequests(Member member) {
        return sharedBoxRequestRepository.findByReceiverAndStatus(member, RequestStatus.PENDING).stream()
                .map(BoxShareRequestResponse::from)
                .toList();
    }

    // 공유 박스 수락 (ACCEPTED)
    @Transactional
    public void acceptSharedBoxRequest(Long requestId) {
        SharedBoxRequest request = sharedBoxRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOX_NOT_FOUND, requestId));
        request.updateStatus(RequestStatus.ACCEPTED);
        sharedBoxRequestRepository.save(request);

        // 공유 박스 생성
        Box box = boxRepository.save(Box.builder()
                .boxType(BoxType.SHARED)
                .build());
        BoxMember boxMemberA = boxMemberRepository.save(BoxMember.builder()
                .box(box)
                .member(request.getReceiver())
                .role(BoxMemberRole.OWNER)
                .build());
        BoxMember boxMemberB = boxMemberRepository.save(BoxMember.builder()
                .box(box)
                .member(request.getSender())
                .role(BoxMemberRole.OWNER)
                .build());

        log.info("Shared box created with BoxID: {}, Member IDs: {} and {}"
                , box.getBoxId(), boxMemberA.getMember().getMemberId(), boxMemberB.getMember().getMemberId());
    }

    // 공유 박스 거절 (REJECTED)
    @Transactional
    public void rejectSharedBoxRequest(Long requestId) {
        SharedBoxRequest request = sharedBoxRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOX_NOT_FOUND, requestId));
        request.updateStatus(RequestStatus.REJECTED);
    }




}
