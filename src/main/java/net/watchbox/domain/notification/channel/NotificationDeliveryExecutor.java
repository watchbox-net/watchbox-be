package net.watchbox.domain.notification.channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.notification.delivery.DeliveryStatus;
import net.watchbox.domain.notification.delivery.NotificationDeliveryRecorder;
import net.watchbox.domain.notification.event.NotificationMessage;
import org.springframework.stereotype.Component;

/**
 * 채널 하나를 <b>멱등하게</b> 실행한다. 로컬 경로와 Kafka 컨슈머가 <b>같은 코드를 쓴다.</b>
 *
 * <p>전송 경로가 달라도 소비 쪽 규칙은 같아야 한다 — 이미 끝난 채널은 건너뛰고, 재시도가 남은
 * 실패만 위로 올린다. 그 규칙을 여기 한 곳에 두어 두 경로가 어긋나지 않게 한다.
 *
 * <p><b>예외를 던지는 기준이 곧 재시도 신호다.</b>
 * <ul>
 *   <li>성공 · 재시도 무의미 · 상한 도달 → 조용히 종료 (더 할 일이 없다)</li>
 *   <li>재시도가 남은 실패 → <b>다시 던진다</b>. 받는 쪽은 로컬이면 outbox 릴레이,
 *       Kafka 면 컨슈머 에러 핸들러다</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDeliveryExecutor {

    private final NotificationDeliveryRecorder deliveryRecorder;

    public void deliver(NotificationChannelHandler handler, NotificationMessage message) {
        if (!handler.supports(message.event())) {
            return;
        }
        String eventId = message.eventId();
        if (deliveryRecorder.isTerminal(eventId, handler.channel())) {
            return; // 이미 보냈거나 이미 포기했다
        }

        try {
            handler.handle(message.event());
            deliveryRecorder.markSent(eventId, handler.channel());
        } catch (NonRetryableDeliveryException e) {
            deliveryRecorder.abandon(eventId, handler.channel(), e);
            log.warn("delivery abandoned, retry is pointless - eventId={}, channel={}, cause={}",
                    eventId, handler.channel(), e.getMessage());
        } catch (RuntimeException e) {
            DeliveryStatus status = deliveryRecorder.markFailed(
                    eventId, handler.channel(), e, handler.maxAttempt());
            if (status == DeliveryStatus.FAILED) {
                log.warn("delivery gave up after max attempts - eventId={}, channel={}, cause={}",
                        eventId, handler.channel(), e.toString());
                return; // 종결됐으니 위로 올리지 않는다
            }
            log.warn("delivery failed, will retry - eventId={}, channel={}, cause={}",
                    eventId, handler.channel(), e.toString());
            throw e;
        }
    }
}
