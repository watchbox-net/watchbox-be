package net.watchbox.domain.notification.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.entity.invitation.RequestStatus;
import net.watchbox.domain.notification.dto.payload.BoxInvitationPayload;
import net.watchbox.domain.notification.dto.payload.BoxInvitationRespondedPayload;
import net.watchbox.domain.notification.dto.payload.ContentBoxAddedPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kafka 로 나갈 봉투가 JSON 왕복에서 <b>원래 타입 그대로</b> 돌아오는지 고정한다.
 *
 * <p>여기가 깨지면 컨슈머가 이벤트를 복원하지 못해 알림이 통째로 멈춘다.
 * 그런데 <b>런타임까지 안 드러난다</b> — 브로커를 켜야 재현되기 때문에 테스트로 막아둔다.
 */
class NotificationMessageSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private NotificationMessage roundTrip(NotificationMessage message) throws Exception {
        String json = objectMapper.writeValueAsString(message);
        return objectMapper.readValue(json, NotificationMessage.class);
    }

    @Test
    @DisplayName("초대 수신 이벤트가 구현 타입까지 복원된다")
    void invitationReceived() throws Exception {
        NotificationMessage original = new NotificationMessage("evt-1",
                new BoxInvitationReceivedEvent(7L,
                        new BoxInvitationPayload(1L, 10L, "주말 영화", 2L, "현", null)));

        NotificationMessage restored = roundTrip(original);

        assertThat(restored).isEqualTo(original);
        assertThat(restored.event()).isInstanceOf(BoxInvitationReceivedEvent.class);
        assertThat(restored.eventId()).isEqualTo("evt-1");
    }

    @Test
    @DisplayName("초대 응답 이벤트가 enum 필드까지 복원된다")
    void invitationResponded() throws Exception {
        NotificationMessage original = new NotificationMessage("evt-2",
                new BoxInvitationRespondedEvent(2L, new BoxInvitationRespondedPayload(
                        1L, 10L, "주말 영화", 3L, "받는이", null, RequestStatus.ACCEPTED)));

        NotificationMessage restored = roundTrip(original);

        assertThat(restored).isEqualTo(original);
        assertThat(restored.event()).isInstanceOf(BoxInvitationRespondedEvent.class);
    }

    @Test
    @DisplayName("수신자가 여럿인 이벤트도 순서까지 복원된다")
    void contentBoxAdded() throws Exception {
        NotificationMessage original = new NotificationMessage("evt-3",
                new ContentBoxAddedEvent(List.of(5L, 6L, 7L), new ContentBoxAddedPayload(
                        10L, "주말 영화", BoxType.SHARED, 100L, 1030571L, "MOVIE",
                        "마지막 흔적", "/poster.jpg", 2L, "현", null)));

        NotificationMessage restored = roundTrip(original);

        assertThat(restored).isEqualTo(original);
        assertThat(restored.event().receiverIds()).containsExactly(5L, 6L, 7L);
    }

    @Test
    @DisplayName("이벤트 타입 판별자가 JSON 에 실린다 — 이게 없으면 컨슈머가 복원할 수 없다")
    void carriesTypeDiscriminator() throws Exception {
        NotificationMessage message = new NotificationMessage("evt-1",
                new BoxInvitationReceivedEvent(7L,
                        new BoxInvitationPayload(1L, 10L, "주말 영화", 2L, "현", null)));

        String json = objectMapper.writeValueAsString(message);

        assertThat(json).contains("\"eventType\":\"BOX_INVITATION_RECEIVED\"");
        assertThat(json).contains("\"type\":\"BOX_INVITATION_RECEIVED\"");   // payload 쪽 판별자
    }

    @Test
    @DisplayName("파생 값(type·partitionKey)은 직렬화에 섞이지 않는다")
    void derivedValuesAreNotSerialized() throws Exception {
        NotificationMessage message = new NotificationMessage("evt-1",
                new BoxInvitationReceivedEvent(7L,
                        new BoxInvitationPayload(1L, 10L, "주말 영화", 2L, "현", null)));

        String json = objectMapper.writeValueAsString(message);

        assertThat(json).doesNotContain("partitionKey");
        assertThat(json).doesNotContain("notificationType");
    }
}
