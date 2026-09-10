package net.watchbox.global.event;

import net.watchbox.domain.notification.dto.payload.BoxInvitationPayload;
import net.watchbox.domain.notification.event.BoxInvitationReceivedEvent;
import net.watchbox.domain.notification.event.NotificationMessage;
import net.watchbox.global.health.EventTransportProbe;
import net.watchbox.global.properties.KafkaTopicProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <b>타입이 다른 메시지가 같은 토픽에 섞이면 안 된다.</b>
 *
 * <p>전역 consumer 가 타입 헤더를 쓰지 않아({@code use.type.headers=false}) 컨슈머는 대상 타입을
 * 고정해서 읽는다. 한 토픽에 종류가 섞이면 남의 메시지를 자기 타입으로 역직렬화하려다 깨지고,
 * <b>그 메시지는 조용히 사라진다.</b> 브로커를 켜야만 드러나므로 여기서 막는다.
 */
class TopicRoutingTest {

    private final KafkaTopicProperties topics =
            new KafkaTopicProperties("dev.healthcheck", "dev.notification-events", "dev.domain-events");

    @Test
    @DisplayName("알림 봉투는 알림 토픽으로 간다")
    void notificationGoesToNotificationTopic() {
        NotificationMessage message = new NotificationMessage("evt-1",
                new BoxInvitationReceivedEvent(7L,
                        new BoxInvitationPayload(1L, 10L, "주말 영화", 2L, "현", null)));

        assertThat(message.topicKey()).isEqualTo(TopicKey.NOTIFICATION_EVENTS);
        assertThat(topics.resolve(message.topicKey())).isEqualTo("dev.notification-events");
    }

    @Test
    @DisplayName("헬스체크 프로브는 도메인 토픽으로 간다 — 기본값")
    void probeGoesToDomainTopic() {
        EventTransportProbe probe = new EventTransportProbe("p-1", "ok");

        assertThat(probe.topicKey()).isEqualTo(TopicKey.DOMAIN_EVENTS);
        assertThat(topics.resolve(probe.topicKey())).isEqualTo("dev.domain-events");
    }

    @Test
    @DisplayName("둘은 서로 다른 토픽이어야 한다 — 섞이면 역직렬화가 깨진다")
    void notificationAndProbeMustNotShareTopic() {
        NotificationMessage message = new NotificationMessage("evt-1",
                new BoxInvitationReceivedEvent(7L,
                        new BoxInvitationPayload(1L, 10L, "주말 영화", 2L, "현", null)));
        EventTransportProbe probe = new EventTransportProbe("p-1", "ok");

        assertThat(topics.resolve(message.topicKey()))
                .isNotEqualTo(topics.resolve(probe.topicKey()));
    }
}
