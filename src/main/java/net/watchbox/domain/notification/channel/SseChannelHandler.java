package net.watchbox.domain.notification.channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.notification.dev.NotificationFailureInjector;
import net.watchbox.domain.notification.dto.response.NotificationResponse;
import net.watchbox.domain.notification.entity.Notification;
import net.watchbox.domain.notification.entity.NotificationChannel;
import net.watchbox.domain.notification.event.NotificationEvent;
import net.watchbox.domain.notification.metrics.NotificationDeliveryMetrics;
import net.watchbox.domain.notification.service.NotificationCommandService;
import net.watchbox.domain.notification.sse.service.SseEmitterService;
import net.watchbox.global.properties.DeliveryProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림함 저장 + SSE 실시간 푸시. <b>모든 알림이 거치는 기본 채널</b>이다.
 *
 * <p>수신자 수만큼 {@code Notification} 행을 만든다(fan-out on write). 오프라인 사용자는 SSE 가
 * no-op 이지만 행은 남아 다음 구독 때 catchup 으로 푸시된다 —
 * <b>이 만회 경로가 있어서 재시도 상한이 메일보다 낮다.</b>
 *
 * <p>자체 트랜잭션을 연다. 릴레이가 감싸지 않으므로 여기가 경계다.
 */
@Slf4j
@Component
@Order(1)   // 빠른 실시간 채널을 먼저 — 순서에 의존하지는 않지만 체감이 낫다
@RequiredArgsConstructor
public class SseChannelHandler implements NotificationChannelHandler {

    private final NotificationCommandService notificationCommandService;
    private final SseEmitterService sseEmitterService;
    private final NotificationDeliveryMetrics deliveryMetrics;
    private final NotificationFailureInjector failureInjector;
    /** 실패해도 알림함 + catchup 이 만회한다. 메일보다 짧게 잡는다. */
    private final DeliveryProperties deliveryProperties;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SSE;
    }

    @Override
    public boolean supports(NotificationEvent event) {
        return true; // 모든 알림은 알림함에 남는다
    }

    @Override
    public int maxAttempt() {
        return deliveryProperties.sseMaxAttempt();
    }

    @Override
    @Transactional
    public void handle(NotificationEvent event) {
        try {
            for (Long receiverId : event.receiverIds()) {
                failureInjector.maybeFail(NotificationChannel.SSE);
                Notification saved = notificationCommandService.createNotification(
                        receiverId, event.notificationType(), event.payload());
                sseEmitterService.send(receiverId, NotificationResponse.from(saved));
                deliveryMetrics.success(NotificationChannel.SSE);
            }
        } catch (RuntimeException e) {
            // 세고 나서 그대로 던진다 — 릴레이가 받아 재시도한다.
            deliveryMetrics.failure(NotificationChannel.SSE);
            throw e;
        }
    }
}
