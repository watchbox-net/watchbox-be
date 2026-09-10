package net.watchbox.global.properties;

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
}
