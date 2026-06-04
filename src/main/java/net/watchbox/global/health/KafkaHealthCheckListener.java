package net.watchbox.global.health;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kafka 발행/구독 헬스체크용 리스너.
 *
 * <p>실서비스와 동일하게 {@code @KafkaListener} 로 소비 → OTel Spring Boot starter 가
 * consumer span 을 자동 계측한다 (raw KafkaConsumer 와 달리 trace 에 소비 구간이 잡힘).
 *
 * <p>동작:
 * <ol>
 *   <li>헬스체크 컨트롤러가 {@link #register(String)} 로 key 별 future 등록</li>
 *   <li>publish 한 메시지를 이 리스너가 수신 → key 매칭되면 future 완료</li>
 *   <li>컨트롤러가 future 로 수신 값 확인</li>
 * </ol>
 * 매칭 안 되는 메시지(다른 인스턴스/오래된 것)는 무시.
 */
@Slf4j
@Component
public class KafkaHealthCheckListener {

    private final Map<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

    /** 해당 key 의 메시지 수신을 기다리는 future 등록. */
    public CompletableFuture<String> register(String key) {
        CompletableFuture<String> future = new CompletableFuture<>();
        pending.put(key, future);
        return future;
    }

    /** 대기 종료 후 정리 (timeout/완료 무관 항상 호출). */
    public void unregister(String key) {
        pending.remove(key);
    }

    @KafkaListener(
            topics = "${kafka.topics.health-check}",
            groupId = "watchbox-healthcheck-${spring.profiles.active:local}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        CompletableFuture<String> future = pending.get(record.key());
        if (future != null) {
            future.complete(record.value());
        }
        // 매칭 안 되면 무시 (다른 인스턴스가 등록한 key 또는 stale 메시지)
    }
}
