package net.watchbox.global.config;

import lombok.RequiredArgsConstructor;
import net.watchbox.global.properties.KafkaTopicProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 관련 설정.
 *
 * <ul>
 *   <li>{@link KafkaTopicProperties} 바인딩 활성화</li>
 *   <li>{@link NewTopic} 빈 등록 — 부팅 시 KafkaAdmin 이 자동으로 토픽 생성/검증</li>
 * </ul>
 *
 * <p>토픽 이름은 yml 에서 {@code ${spring.profiles.active}.xxx} 패턴으로 환경별 prefix 자동 적용.
 */
@Configuration
@EnableConfigurationProperties(KafkaTopicProperties.class)
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaTopicProperties topics;

    /** 헬스체크용 토픽 — partition 1, replica 1, 짧은 retention 권장 (브로커 측 설정). */
    @Bean
    public NewTopic healthCheckTopic() {
        return TopicBuilder.name(topics.healthCheck())
                .partitions(1)
                .replicas(1)
                .build();
    }

    /** 알림 이벤트 토픽 — 사용자별 순서 보장을 위해 memberId 키로 파티셔닝, partition 3. */
    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name(topics.notificationEvents())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
