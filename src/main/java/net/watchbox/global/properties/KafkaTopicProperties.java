package net.watchbox.global.properties;

import net.watchbox.global.event.TopicKey;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml 의 {@code kafka.topics.*} 바인딩.
 * 환경별 prefix(dev./prod./local.) 는 yml 에서 처리 — 코드에선 토픽명만 참조.
 */
@ConfigurationProperties("kafka.topics")
public record KafkaTopicProperties(
        String healthCheck,
        String notificationEvents,
        String domainEvents
) {

    /** 논리 토픽 → 실제 이름. switch 가 enum 을 전부 다뤄 새 토픽을 추가하면 컴파일이 막힌다. */
    public String resolve(TopicKey key) {
        return switch (key) {
            case DOMAIN_EVENTS -> domainEvents;
            case NOTIFICATION_EVENTS -> notificationEvents;
        };
    }
}
