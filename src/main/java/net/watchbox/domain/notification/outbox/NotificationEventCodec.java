package net.watchbox.domain.notification.outbox;

import net.watchbox.domain.notification.dto.payload.BoxInvitationPayload;
import net.watchbox.domain.notification.dto.payload.BoxInvitationRespondedPayload;
import net.watchbox.domain.notification.dto.payload.ContentBoxAddedPayload;
import net.watchbox.domain.notification.event.BoxInvitationReceivedEvent;
import net.watchbox.domain.notification.event.BoxInvitationRespondedEvent;
import net.watchbox.domain.notification.event.ContentBoxAddedEvent;
import net.watchbox.domain.notification.event.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 저장된 outbox 행 → 원래의 {@link NotificationEvent} 복원.
 *
 * <p>이벤트 전체를 JSON 으로 굽지 않고 <b>타입 + 수신자 + payload 만 저장하고 여기서 조립</b>한다.
 * payload 직렬화는 이미 검증된 {@code NotificationPayloadConverter} 를 그대로 쓰고,
 * 복원 규칙은 이 switch 한 곳에만 둔다. 이벤트 클래스에 직렬화용 애노테이션이 번지지 않는다.
 *
 * <p>switch 가 enum 을 전부 다루므로 <b>새 알림 타입을 추가하면 컴파일이 막힌다</b>.
 */
@Component
public class NotificationEventCodec {

    public NotificationEvent toEvent(OutboxEvent row) {
        List<Long> receivers = row.receiverIdList();
        return switch (row.getNotificationType()) {
            case BOX_INVITATION_RECEIVED -> new BoxInvitationReceivedEvent(
                    receivers.getFirst(), (BoxInvitationPayload) row.getPayload());
            case BOX_INVITATION_RESPONDED -> new BoxInvitationRespondedEvent(
                    receivers.getFirst(), (BoxInvitationRespondedPayload) row.getPayload());
            case BOX_CONTENT_ADDED -> new ContentBoxAddedEvent(
                    receivers, (ContentBoxAddedPayload) row.getPayload());
        };
    }
}
