package net.watchbox.domain.box.service.invitation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        boxInvitation.updateStatus(RequestStatus.ACCEPTED);
    }

    // 공유 박스 받은초대 거절 (REJECTED)
    @Transactional
    public void rejectBoxInvitation(Member receiver, BoxInvitation boxInvitation) {
        boxInvitation.updateStatus(RequestStatus.REJECTED);
    }

    // 공유 박스 보낸초대 리스트 조회
    public List<BoxInvitation> getAllBySender(Member sender) {
        return boxInvitationRepository.findAllBySender(sender);
    }

    // 공유 박스 받은초대 리스트 조회
    public List<BoxInvitation> getAllByReceiver(Member receiver) {
        return boxInvitationRepository.findAllByReceiver(receiver);
    }

    @Transactional
    public void deleteBoxInvitation(BoxInvitation boxInvitation) {
        boxInvitationRepository.delete(boxInvitation);
    }
}
