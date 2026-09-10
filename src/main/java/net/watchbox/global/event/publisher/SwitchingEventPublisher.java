package net.watchbox.global.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.global.event.DomainEvent;
import net.watchbox.global.event.DomainEventPublisher;
import net.watchbox.global.event.EventTransport;
import net.watchbox.global.event.setting.EventTransportSettings;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionException;

/**
 * 설정값에 따라 로컬/Kafka 로 위임하는 발행자. 발행부가 주입받는 것은 이 구현이다.
 *
 * <p><b>설정 조회는 캐시에서만 한다.</b> 이벤트마다 DB 를 치면 발행 경로가 곧 병목이자
 * 장애점이 된다. ({@link EventTransportSettings#current()})
 *
 * <p><b>Kafka 실패 시 로컬로 폴백한다.</b> 브로커가 꺼져 있어도 이벤트를 통째로 잃지 않기
 * 위해서다. 대신 이 설계는 엄밀히는 "스위치"가 아니라 "우선순위"가 된다 —
 * KAFKA 모드가 곧 "가능하면 Kafka, 안 되면 로컬"이라는 뜻임을 알고 써야 한다.
 *
 * <p><b>브로커 ACK 까지 기다린다.</b> 프로듀서 버퍼에 넣은 것만으로 성공 처리하면,
 * 호출자(outbox 릴레이)가 행을 닫은 뒤에 전송이 실패할 수 있다. 그러면 되돌릴 방법이 없다.
 * 기다려야 {@code published_at} 이 <b>"브로커가 받았다"</b> 를 뜻하게 된다.
 * 릴레이는 가상 스레드에서 돌아 블로킹 비용이 작다.
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class SwitchingEventPublisher implements DomainEventPublisher {

    private final LocalEventPublisher localEventPublisher;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final EventTransportSettings settings;

    @Override
    public void publish(DomainEvent event) {
        if (settings.current() != EventTransport.KAFKA) {
            localEventPublisher.publish(event);
            return;
        }
        publishViaKafkaWithFallback(event);
    }

    private void publishViaKafkaWithFallback(DomainEvent event) {
        try {
            // 동기 실패(브로커 미가용)는 send() 가 max.block.ms 후 던지고,
            // 비동기 실패(전송 중 브로커 다운 등)는 join() 이 받는다. 두 경로를 한 곳에서 처리한다.
            kafkaEventPublisher.send(event).join();
        } catch (Exception e) {
            fallbackToLocal(event, unwrap(e));
        }
    }

    /** join() 이 감싸는 CompletionException 을 벗겨 로그에 진짜 원인이 남게 한다. */
    private Throwable unwrap(Throwable e) {
        return (e instanceof CompletionException && e.getCause() != null) ? e.getCause() : e;
    }

    private void fallbackToLocal(DomainEvent event, Throwable cause) {
        log.warn("kafka publish failed, falling back to local - type={}, key={}, cause={}",
                event.type(), event.partitionKey(), cause.getMessage());
        localEventPublisher.publish(event);
    }
}
