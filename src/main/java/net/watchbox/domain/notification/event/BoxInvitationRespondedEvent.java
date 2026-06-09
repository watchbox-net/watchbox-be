package net.watchbox.domain.notification.event;

import net.watchbox.domain.notification.dto.payload.BoxInvitationRespondedPayload;
import net.watchbox.domain.notification.entity.NotificationType;

import java.util.List;

/**
 * 공유 박스 초대를 받은 사용자가 수락/거절했을 때 발행.
 * 수신자 = 원래 초대를 보냈던 sender 1명.
 *
 * <p>발행 위치: BoxInvitationFacade.acceptBoxInvitation / rejectBoxInvitation 트랜잭션 안.
 */
public record BoxInvitationRespondedEvent(
        Long senderId,                          // 알림 수신자 (= 원래 초대 sender)
        BoxInvitationRespondedPayload payload
) implements NotificationEvent {

    @Override
    public List<Long> receiverIds() {
        return List.of(senderId);
    }

    @Override
    public NotificationType type() {
        return NotificationType.BOX_INVITATION_RESPONDED;
    }
}
