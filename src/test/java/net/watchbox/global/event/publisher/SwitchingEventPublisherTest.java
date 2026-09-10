package net.watchbox.global.event.publisher;

import net.watchbox.global.event.DomainEvent;
import net.watchbox.global.event.EventTransport;
import net.watchbox.global.event.setting.EventTransportSettings;
import net.watchbox.global.health.EventTransportProbe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * <b>브로커가 죽어도 이벤트를 잃지 않는다</b> 는 성질을 고정한다.
 *
 * <p>Kafka 는 비용 때문에 평소 꺼두는 구조라, 켜져 있다고 가정한 코드는 언젠가 반드시 물린다.
 * 실패가 동기(브로커 미가용)로 오든 비동기(전송 중 다운)로 오든 <b>같은 자리에서 로컬로 떨어져야</b> 한다.
 */
class SwitchingEventPublisherTest {

    private final LocalEventPublisher local = mock(LocalEventPublisher.class);
    private final KafkaEventPublisher kafka = mock(KafkaEventPublisher.class);
    private final EventTransportSettings settings = mock(EventTransportSettings.class);

    private final SwitchingEventPublisher publisher =
            new SwitchingEventPublisher(local, kafka, settings);

    private final DomainEvent event = new EventTransportProbe("p-1", "ok");

    private void given(EventTransport transport) {
        when(settings.current()).thenReturn(transport);
    }

    @Test
    @DisplayName("LOCAL 이면 브로커를 건드리지 않는다")
    void localDoesNotTouchKafka() {
        given(EventTransport.LOCAL);

        publisher.publish(event);

        verify(local).publish(event);
        verifyNoInteractions(kafka);
    }

    @Test
    @DisplayName("KAFKA 이고 ACK 를 받으면 로컬로 중복 발행하지 않는다")
    void kafkaSuccessDoesNotAlsoPublishLocally() {
        given(EventTransport.KAFKA);
        when(kafka.send(event)).thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        publisher.publish(event);

        verify(kafka).send(event);
        verifyNoInteractions(local);
    }

    @Test
    @DisplayName("브로커가 꺼져 있어(동기 실패) 발행이 던지면 로컬로 떨어진다")
    void fallsBackOnSynchronousFailure() {
        given(EventTransport.KAFKA);
        when(kafka.send(any())).thenThrow(new IllegalStateException("broker unreachable"));

        publisher.publish(event);

        verify(local).publish(event);
    }

    @Test
    @DisplayName("전송 중 실패(비동기)해도 로컬로 떨어진다 — ACK 를 기다리기 때문에 잡힌다")
    void fallsBackOnAsynchronousFailure() {
        given(EventTransport.KAFKA);
        when(kafka.send(event)).thenReturn(
                CompletableFuture.failedFuture(new IllegalStateException("broker went down")));

        publisher.publish(event);

        verify(local).publish(event);
    }

    @Test
    @DisplayName("설정 조회는 이벤트당 한 번만 — 발행 경로에 DB 조회가 끼면 안 된다")
    void readsTransportSettingOncePerEvent() {
        given(EventTransport.LOCAL);

        publisher.publish(event);

        verify(settings, times(1)).current();
    }
}
