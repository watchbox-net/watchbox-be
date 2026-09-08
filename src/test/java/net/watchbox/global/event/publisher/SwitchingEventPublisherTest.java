package net.watchbox.global.event.publisher;

import net.watchbox.global.event.DomainEvent;
import net.watchbox.global.event.EventTransport;
import net.watchbox.global.event.setting.EventTransportSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SwitchingEventPublisherTest {

    private record TestEvent(String partitionKey) implements DomainEvent {
        public String type() { return "test"; }
    }

    private LocalEventPublisher local;
    private KafkaEventPublisher kafka;
    private EventTransportSettings settings;
    private SwitchingEventPublisher publisher;
    private final DomainEvent event = new TestEvent("member-1");

    @BeforeEach
    void setUp() {
        local = mock(LocalEventPublisher.class);
        kafka = mock(KafkaEventPublisher.class);
        settings = mock(EventTransportSettings.class);
        publisher = new SwitchingEventPublisher(local, kafka, settings);
    }

    @Test
    @DisplayName("LOCAL 모드면 Kafka 를 건드리지 않는다")
    void local모드면_kafka_미사용() {
        when(settings.current()).thenReturn(EventTransport.LOCAL);

        publisher.publish(event);

        verify(local).publish(event);
        verify(kafka, never()).send(any());
    }

    @Test
    @DisplayName("KAFKA 모드면 Kafka 로 발행한다")
    void kafka모드면_kafka로_발행() {
        when(settings.current()).thenReturn(EventTransport.KAFKA);
        when(kafka.send(event)).thenReturn(CompletableFuture.completedFuture(null));

        publisher.publish(event);

        verify(kafka).send(event);
        verify(local, never()).publish(any());
    }

    @Test
    @DisplayName("브로커 미가용으로 send 가 예외를 던지면 로컬로 폴백한다 (동기 실패)")
    void 동기실패면_로컬폴백() {
        when(settings.current()).thenReturn(EventTransport.KAFKA);
        when(kafka.send(event)).thenThrow(new RuntimeException("broker down"));

        assertThatCode(() -> publisher.publish(event)).doesNotThrowAnyException();
        verify(local).publish(event);
    }

    @Test
    @DisplayName("발행 future 가 실패해도 로컬로 폴백한다 (비동기 실패)")
    void 비동기실패면_로컬폴백() {
        when(settings.current()).thenReturn(EventTransport.KAFKA);
        when(kafka.send(event)).thenReturn(CompletableFuture.failedFuture(new RuntimeException("send failed")));

        publisher.publish(event);

        verify(local).publish(event);
    }

    @Test
    @DisplayName("설정 조회는 이벤트당 한 번만 — 발행 경로에 DB 조회가 끼면 안 된다")
    void 설정조회는_이벤트당_한번() {
        when(settings.current()).thenReturn(EventTransport.LOCAL);

        publisher.publish(event);

        verify(settings, times(1)).current();
    }
}
