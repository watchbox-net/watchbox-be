package net.watchbox.global.event.publisher;

import lombok.RequiredArgsConstructor;
import net.watchbox.global.event.DomainEvent;
import net.watchbox.global.event.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 내부 이벤트로 발행한다. 브로커가 필요 없어 기본 경로로 쓴다.
 *
 * <p>한계: 같은 JVM 안에서만 전달되고, 종료 시 처리 전 이벤트는 사라진다.
 * (다중 인스턴스 전달·유실 방지가 필요해지는 지점이 Kafka 로 넘어갈 이유다)
 */
@Component
@RequiredArgsConstructor
public class LocalEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
