package net.watchbox.domain.notification.event;

import net.watchbox.domain.notification.dto.payload.BoxInvitationPayload;
import net.watchbox.domain.notification.entity.NotificationType;

import java.util.List;

/**
 * 공유 박스 초대 발송 시 발행.
 * 수신자 = 초대받은 멤버 1명.
 *
 * <p>발행 위치 예: BoxInvitationFacade.createInvitation(...) 트랜잭션 안.
 */
public record BoxInvitationReceivedEvent(
        Long receiverId,
        BoxInvitationPayload payload
) implements NotificationEvent {

    @Override
    public List<Long> receiverIds() {
        return List.of(receiverId);
    }

    @Override
    public NotificationType notificationType() {
        return NotificationType.BOX_INVITATION_RECEIVED;
    }
}
