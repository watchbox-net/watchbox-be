package net.watchbox.domain.notification.outbox;

import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.entity.invitation.RequestStatus;
import net.watchbox.domain.notification.dto.payload.BoxInvitationPayload;
import net.watchbox.domain.notification.dto.payload.BoxInvitationRespondedPayload;
import net.watchbox.domain.notification.dto.payload.ContentBoxAddedPayload;
import net.watchbox.domain.notification.entity.NotificationType;
import net.watchbox.domain.notification.event.BoxInvitationReceivedEvent;
import net.watchbox.domain.notification.event.BoxInvitationRespondedEvent;
import net.watchbox.domain.notification.event.ContentBoxAddedEvent;
import net.watchbox.domain.notification.event.NotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * outbox 왕복이 이벤트를 <b>원래 타입 그대로</b> 복원하는지 고정한다.
 * 여기가 깨지면 리스너가 이벤트를 못 받아 알림이 조용히 사라진다.
 */
class NotificationEventCodecTest {

    private final NotificationEventCodec codec = new NotificationEventCodec();

    /** OutboxEvent.from() 은 payload 를 그대로 들고 있으므로 컨버터 없이 왕복을 검증할 수 있다. */
    private NotificationEvent roundTrip(NotificationEvent event) {
        return codec.toEvent(OutboxEvent.from(event));
    }

    @Test
    @DisplayName("초대 수신 이벤트가 그대로 복원된다")
    void invitationReceived() {
        BoxInvitationPayload payload =
                new BoxInvitationPayload(1L, 10L, "주말 영화", 2L, "현", null);
        BoxInvitationReceivedEvent original = new BoxInvitationReceivedEvent(7L, payload);

        NotificationEvent restored = roundTrip(original);

        assertThat(restored).isInstanceOf(BoxInvitationReceivedEvent.class).isEqualTo(original);
        assertThat(restored.receiverIds()).containsExactly(7L);
        assertThat(restored.notificationType()).isEqualTo(NotificationType.BOX_INVITATION_RECEIVED);
    }

    @Test
    @DisplayName("초대 응답 이벤트가 그대로 복원된다")
    void invitationResponded() {
        BoxInvitationRespondedPayload payload = new BoxInvitationRespondedPayload(
                1L, 10L, "주말 영화", 3L, "받는이", null, RequestStatus.ACCEPTED);
        BoxInvitationRespondedEvent original = new BoxInvitationRespondedEvent(2L, payload);

        NotificationEvent restored = roundTrip(original);

        assertThat(restored).isInstanceOf(BoxInvitationRespondedEvent.class).isEqualTo(original);
        assertThat(restored.receiverIds()).containsExactly(2L);
    }

    @Test
    @DisplayName("수신자가 여럿인 콘텐츠 추가 이벤트도 순서까지 복원된다")
    void contentBoxAdded() {
        ContentBoxAddedPayload payload = new ContentBoxAddedPayload(
                10L, "주말 영화", BoxType.SHARED, 100L, 1030571L, "MOVIE",
                "마지막 흔적", "/poster.jpg", 2L, "현", null);
        ContentBoxAddedEvent original = new ContentBoxAddedEvent(List.of(5L, 6L, 7L), payload);

        NotificationEvent restored = roundTrip(original);

        assertThat(restored).isInstanceOf(ContentBoxAddedEvent.class).isEqualTo(original);
        assertThat(restored.receiverIds()).containsExactly(5L, 6L, 7L);
    }

    @Test
    @DisplayName("DomainEvent 로서의 type/partitionKey 가 알림 정보에서 파생된다")
    void domainEventView() {
        NotificationEvent event = new BoxInvitationReceivedEvent(
                7L, new BoxInvitationPayload(1L, 10L, "주말 영화", 2L, "현", null));

        assertThat(event.type()).isEqualTo("BOX_INVITATION_RECEIVED");
        assertThat(event.partitionKey()).isEqualTo("7");
    }
}
