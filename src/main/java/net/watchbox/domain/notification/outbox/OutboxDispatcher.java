package net.watchbox.domain.notification.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.notification.channel.NonRetryableDeliveryException;
import net.watchbox.domain.notification.channel.NotificationChannelHandler;
import net.watchbox.domain.notification.delivery.DeliveryStatus;
import net.watchbox.domain.notification.delivery.NotificationDeliveryRecorder;
import net.watchbox.domain.notification.event.NotificationEvent;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * outbox 행 하나를 <b>채널별로</b> 전달한다.
 *
 * <p><b>여기에는 트랜잭션이 없다.</b> 채널마다 자기 트랜잭션을 열고, 결과 기록은 별도 트랜잭션으로
 * 남는다. 하나로 묶으면 메일 실패가 이미 성공한 SSE 까지 롤백시켜 <b>재시도 때 SSE 푸시가 또 나간다.</b>
 *
 * <p>이미 끝난 채널은 건너뛴다. 그래서 <b>재시도해도 남은 채널만 다시 시도</b>한다.
 * 채널 실행 순서에 의존하지 않으므로, 순서가 바뀌어도 중복이 생기지 않는다.
 *
 * <p>채널이 하나라도 재시도 대기로 남으면 행을 미발행으로 두고 backoff 후 다시 집는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxDispatcher {

    private final OutboxEventRepository outboxEventRepository;
    private final NotificationEventCodec notificationEventCodec;
    private final NotificationDeliveryRecorder deliveryRecorder;
    private final OutboxStateWriter outboxStateWriter;

    /** 등록된 모든 채널. 어떤 이벤트를 받을지는 각 핸들러의 {@code supports} 가 정한다. */
    private final List<NotificationChannelHandler> channelHandlers;

    public void dispatch(Long outboxId) {
        OutboxEvent row = outboxEventRepository.findById(outboxId).orElse(null);
        if (row == null || row.getPublishedAt() != null) {
            return; // 이미 처리됐다. 중복으로 집히는 것은 정상 상황이라 조용히 넘어간다.
        }

        NotificationEvent event = notificationEventCodec.toEvent(row);
        String eventId = row.getEventId();
        boolean anyPending = false;
        String lastError = null;

        for (NotificationChannelHandler handler : channelHandlers) {
            if (!handler.supports(event) || deliveryRecorder.isTerminal(eventId, handler.channel())) {
                continue;
            }
            String error = deliverOnce(handler, event, eventId);
            if (error != null) {
                anyPending = true;
                lastError = error;
            }
        }

        if (anyPending) {
            outboxStateWriter.markRetryLater(outboxId, lastError);
        } else {
            outboxStateWriter.markPublished(outboxId);
        }
    }

    /**
     * @return 재시도로 남은 경우의 오류 문자열, 더 할 일이 없으면 {@code null}
     *         (성공했거나, 재시도해도 소용없어 종결됐거나, 상한에 닿았거나)
     */
    private String deliverOnce(NotificationChannelHandler handler, NotificationEvent event, String eventId) {
        try {
            handler.handle(event);
            deliveryRecorder.markSent(eventId, handler.channel());
            return null;
        } catch (NonRetryableDeliveryException e) {
            deliveryRecorder.abandon(eventId, handler.channel(), e);
            log.warn("delivery abandoned, retry is pointless - eventId={}, channel={}, cause={}",
                    eventId, handler.channel(), e.getMessage());
            return null;
        } catch (Exception e) {
            DeliveryStatus status = deliveryRecorder.markFailed(
                    eventId, handler.channel(), e, handler.maxAttempt());
            if (status == DeliveryStatus.FAILED) {
                log.warn("delivery gave up after max attempts - eventId={}, channel={}, cause={}",
                        eventId, handler.channel(), e.toString());
                return null;
            }
            log.warn("delivery failed, will retry - eventId={}, channel={}, cause={}",
                    eventId, handler.channel(), e.toString());
            return String.valueOf(e);
        }
    }

    /** 릴레이가 예상 못 한 실패를 만났을 때(행 조회 실패 등) 다음 주기로 미룬다. */
    public void recordFailure(Long outboxId, Throwable cause) {
        outboxStateWriter.markRetryLater(outboxId, String.valueOf(cause));
    }
}
