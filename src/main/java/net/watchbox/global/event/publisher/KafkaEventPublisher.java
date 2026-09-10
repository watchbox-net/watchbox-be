package net.watchbox.global.event.publisher;

import lombok.RequiredArgsConstructor;
import net.watchbox.global.event.DomainEvent;
import net.watchbox.global.event.DomainEventPublisher;
import net.watchbox.global.properties.KafkaTopicProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka 토픽으로 발행한다. 파티션 키로 {@link DomainEvent#partitionKey()} 를 써서
 * 같은 키(예: memberId)의 이벤트 순서를 보장한다.
 *
 * <p><b>실패는 여기서 처리하지 않는다.</b> 폴백 여부는 전송 경로를 아는
 * {@code SwitchingEventPublisher} 의 책임이라 future 를 그대로 넘긴다.
 *
 * <p>브로커가 꺼져 있으면 {@code send()} 자체가 {@code max.block.ms} 만큼 블로킹한 뒤
 * 예외를 던진다. 그래서 설정에서 그 값을 짧게(3초) 줄여둬야 한다 — 기본값 60초로는
 * 요청이 1분간 멈춘다.
 */
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements DomainEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicProperties topics;

    /** 발행 결과 future 를 반환한다. 동기 실패(브로커 미가용)는 예외로 던져진다. */
    public CompletableFuture<SendResult<String, Object>> send(DomainEvent event) {
        return kafkaTemplate.send(topics.domainEvents(), event.partitionKey(), event);
    }

    @Override
    public void publish(DomainEvent event) {
        send(event);
    }
}
