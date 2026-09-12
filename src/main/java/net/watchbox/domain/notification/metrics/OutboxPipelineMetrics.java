package net.watchbox.domain.notification.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.notification.delivery.DeliveryStatus;
import net.watchbox.domain.notification.delivery.NotificationDeliveryRepository;
import net.watchbox.domain.notification.outbox.OutboxEventRepository;
import net.watchbox.global.properties.OutboxProperties;
import org.springframework.stereotype.Component;

/**
 * 알림 파이프라인이 <b>막혔는지</b>를 보는 지표.
 *
 * <p>{@code notification.delivery} 카운터가 "무슨 일이 있었나" 라면, 여기는 "지금 어떤 상태인가" 다.
 * 카운터만으로는 <b>쌓이고 있는 중</b>인지 알 수 없다 — 실패가 안 늘어도 발행이 멈춰 있을 수 있다.
 *
 * <p>게이지는 수집 시점에 COUNT 쿼리를 한 번 돈다. push 주기가 30초라 부담이 없다.
 */
@Component
@RequiredArgsConstructor
public class OutboxPipelineMetrics {

    private final MeterRegistry meterRegistry;
    private final OutboxEventRepository outboxEventRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final OutboxProperties outboxProperties;

    @PostConstruct
    void register() {
        // 미발행 적체. 평상시 0 에 가깝고, 우상향하면 발행이 막혔다는 뜻이다.
        Gauge.builder("notification.outbox.pending", outboxEventRepository,
                        OutboxEventRepository::countByPublishedAtIsNull)
                .description("발행 대기 중인 outbox 행 수")
                .register(meterRegistry);

        // 재시도 상한을 넘겨 더 집히지 않는 행. 0 이 아니면 사람이 봐야 한다.
        Gauge.builder("notification.outbox.exhausted", this,
                        m -> m.outboxEventRepository.countByPublishedAtIsNullAndAttemptGreaterThanEqual(
                                m.outboxProperties.maxAttempt()))
                .description("재시도 상한에 걸려 멈춘 outbox 행 수")
                .register(meterRegistry);

        // 종결된 채널 실패. 주소 없음 같은 정상 종결도 포함되므로 추세로 본다.
        Gauge.builder("notification.delivery.failed", notificationDeliveryRepository,
                        r -> r.countByStatus(DeliveryStatus.FAILED))
                .description("종결된(재시도하지 않는) 채널 전달 실패 수")
                .register(meterRegistry);
    }
}
