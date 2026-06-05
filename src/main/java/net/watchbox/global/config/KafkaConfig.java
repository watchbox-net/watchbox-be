package net.watchbox.global.config;

import lombok.RequiredArgsConstructor;
import net.watchbox.global.properties.KafkaTopicProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

/**
 * Kafka 관련 설정.
 *
 * <ul>
 *   <li>{@link KafkaTopicProperties} 바인딩 활성화</li>
 *   <li>{@link NewTopic} 빈 등록 — 부팅 시 KafkaAdmin 이 자동으로 토픽 생성/검증</li>
 *   <li>헬스체크 전용 리스너 컨테이너 팩토리 (String 값 역직렬화)</li>
 * </ul>
 *
 * <p>토픽 이름은 yml 에서 {@code ${spring.profiles.active}.xxx} 패턴으로 환경별 prefix 자동 적용.
 */
@Configuration
@EnableConfigurationProperties(KafkaTopicProperties.class)
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaTopicProperties topics;

    /** 헬스체크용 토픽 — partition 1, replica 3 (브로커 min.insync.replicas=2 와 일치). */
    @Bean
    public NewTopic healthCheckTopic() {
        return TopicBuilder.name(topics.healthCheck())
                .partitions(1)
                .replicas(3)
                .build();
    }

    /** 알림 이벤트 토픽 — 사용자별 순서 보장을 위해 memberId 키로 파티셔닝, partition 3, replica 3. */
    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name(topics.notificationEvents())
                .partitions(3)
                .replicas(3)
                .build();
    }

    /**
     * 헬스체크 전용 리스너 컨테이너 팩토리.
     *
     * <p>글로벌 consumer 는 알림 객체용 {@link JsonDeserializer}(타입 헤더 미사용) 라
     * 타입 정보 없는 단순 String 헬스체크 메시지를 안정적으로 역직렬화하지 못한다.
     * 여기서는 value 를 {@code JsonDeserializer<String>} 으로 명시해, producer 의
     * {@code JsonSerializer}("ok-123" → "\"ok-123\"") 와 round-trip 으로 정확히 맞춘다.
     *
     * <p>{@code auto.offset.reset=latest}: 헬스체크는 "방금 발행한" 메시지만 관심.
     * 리스너는 상시 가동 중이므로 누적된 과거 메시지를 따라잡느라 타임아웃 날 일이 없다.
     *
     * <p>value 는 {@link ErrorHandlingDeserializer} 로 {@code JsonDeserializer<String>} 을 감싼다.
     * 역직렬화 실패 시 예외를 record 헤더에 담고 value 를 null 로 넘겨서, {@code DefaultErrorHandler}
     * 가 해당 레코드를 건너뛸 수 있게 한다. 이게 없으면 깨진 메시지 하나가 같은 offset 에서
     * 무한 재시도(SerializationException)되어 토픽 소비가 통째로 멈춘다. (실제로 한 번 겪음)
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> healthCheckKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // JsonDeserializer<String>(String.class) 로 타입 명시 → "\"ok-123\"" 를 "ok-123" 으로 복원
        // ErrorHandlingDeserializer 로 감싸 역직렬화 실패가 무한루프로 번지지 않게 함
        ErrorHandlingDeserializer<String> valueDeserializer =
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(String.class, false));

        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        props, new StringDeserializer(), valueDeserializer);

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
