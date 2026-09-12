package net.watchbox.global.config;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.notification.event.NotificationMessage;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import net.watchbox.global.health.EventTransportProbe;
import net.watchbox.global.properties.KafkaTopicProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
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
@RequiredArgsConstructor
public class KafkaConfig {

    /** Spring Kafka 관례. 원본 토픽 이름 뒤에 붙인다. */
    private static final String DLT_SUFFIX = ".DLT";

    /**
     * 컨슈머 재시도 간격. 블로킹 재시도라 <b>그 그룹의 파티션이 그동안 멈춘다</b> —
     * 상한을 짧게 둬서 한 건이 뒤를 오래 막지 않게 한다.
     * (막히는 것은 해당 채널 그룹뿐이고, 다른 채널 오프셋은 계속 전진한다)
     */
    private static final long RETRY_INITIAL_INTERVAL_MS = 2_000L;
    private static final long RETRY_MAX_INTERVAL_MS = 10_000L;
    private static final double RETRY_MULTIPLIER = 2.0;

    /**
     * Kafka 재시도 횟수. <b>채널 재시도 상한(notification.delivery.*)보다 넉넉해야 한다.</b>
     *
     * <p>채널 상한에 먼저 닿으면 {@code NotificationDeliveryExecutor} 가 예외를 던지지 않아
     * Kafka 는 성공으로 보고 오프셋을 넘긴다 — 그게 정상 종료 경로다.
     * 반대로 Kafka 가 먼저 포기하면 메시지는 DLQ 로 가는데 delivery 는 PENDING 으로 남아
     * <b>아무도 재시도하지 않는 상태</b>가 된다.
     */
    private static final int RETRY_MAX_ATTEMPTS = 12;

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

    /** 도메인 이벤트 토픽 — 파티션 키(memberId 등)로 순서 보장, partition 3, replica 3. */
    /**
     * 알림 DLQ. <b>정상적인 채널 실패는 여기 오지 않는다</b> — 그건 notification_delivery 가
     * 재시도 상한까지 관리한다. 여기 쌓이는 건 역직렬화 실패(poison pill)나 예상 못 한 버그다.
     *
     * <p>파티션 수를 원본과 맞춘다. 적으면 같은 파티션으로 보내려다 실패할 수 있다.
     */
    @Bean
    public NewTopic notificationEventsDeadLetterTopic() {
        return TopicBuilder.name(topics.notificationEvents() + DLT_SUFFIX)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic domainEventsTopic() {
        return TopicBuilder.name(topics.domainEvents())
                .partitions(3)
                .replicas(3)
                .build();
    }

    /**
     * 도메인 이벤트 리스너 컨테이너 팩토리.
     *
     * <p>글로벌 consumer 는 {@code spring.json.use.type.headers=false} 라 타입 정보를 헤더에서
     * 얻지 못한다. 여기서 대상 타입을 명시해 역직렬화를 성립시킨다.
     *
     * <p>지금은 프로브 한 종류만 다루므로 단일 타입으로 고정했다. 실제 도메인 이벤트를 여러 종류
     * 태우려면 타입 헤더를 켜거나 이벤트별 토픽을 나누는 결정이 먼저 필요하다.
     *
     * <p>{@link ErrorHandlingDeserializer} 로 감싸는 이유는 헬스체크 팩토리와 같다 —
     * 깨진 메시지 하나가 같은 offset 에서 무한 재시도되어 소비가 멈추는 것을 막는다.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventTransportProbe>
            domainEventKafkaListenerContainerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);

        ErrorHandlingDeserializer<EventTransportProbe> valueDeserializer =
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(EventTransportProbe.class, false));

        DefaultKafkaConsumerFactory<String, EventTransportProbe> consumerFactory =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);

        ConcurrentKafkaListenerContainerFactory<String, EventTransportProbe> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
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

    /**
     * 알림 봉투 리스너 컨테이너 팩토리. <b>채널별 컨슈머 그룹이 이 팩토리를 공유한다.</b>
     *
     * <p>그룹은 리스너마다 {@code groupId} 로 지정한다 — 팩토리를 나눌 필요는 없다.
     * 같은 토픽을 서로 다른 그룹이 읽으면 <b>각자 오프셋을 갖고 전원이 모든 메시지를 받는다.</b>
     * (같은 그룹으로 묶으면 한쪽만 받아 다른 채널이 통째로 죽는다)
     *
     * <p>{@link ErrorHandlingDeserializer} 로 감싸는 이유는 다른 팩토리와 같다 —
     * 깨진 메시지 하나가 같은 offset 에서 무한 재시도되어 소비가 멈추는 것을 막는다.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationMessage>
            notificationMessageKafkaListenerContainerFactory(KafkaProperties kafkaProperties,
                                                             KafkaTemplate<String, Object> kafkaTemplate) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);

        ErrorHandlingDeserializer<NotificationMessage> valueDeserializer =
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(NotificationMessage.class, false));

        DefaultKafkaConsumerFactory<String, NotificationMessage> consumerFactory =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);

        ConcurrentKafkaListenerContainerFactory<String, NotificationMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(notificationErrorHandler(kafkaTemplate));
        return factory;
    }

    /**
     * 재시도 후에도 안 되면 DLQ 로 보낸다. <b>소비가 멈추지 않게 하는 것이 목적</b>이다.
     *
     * <p>이게 없으면 깨진 메시지 하나가 같은 오프셋에서 무한 재시도되어 그 그룹의 소비가 통째로 선다.
     *
     * <p>파티션을 {@code -1} 로 두어 Kafka 가 고르게 한다. 원본 파티션 번호를 그대로 쓰면
     * DLQ 파티션 수가 다를 때 발행이 실패한다.
     */
    private DefaultErrorHandler notificationErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + DLT_SUFFIX, -1));

        ExponentialBackOff backOff = new ExponentialBackOff(RETRY_INITIAL_INTERVAL_MS, RETRY_MULTIPLIER);
        backOff.setMaxInterval(RETRY_MAX_INTERVAL_MS);
        backOff.setMaxAttempts(RETRY_MAX_ATTEMPTS);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
