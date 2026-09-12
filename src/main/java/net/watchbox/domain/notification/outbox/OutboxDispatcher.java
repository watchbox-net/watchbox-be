package net.watchbox.domain.notification.outbox;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.notification.event.NotificationMessage;
import net.watchbox.global.event.DomainEventPublisher;
import org.springframework.stereotype.Service;

/**
 * outbox 행 하나를 전송 계층으로 넘긴다.
 *
 * <p><b>여기에는 트랜잭션이 없다.</b> 채널마다 자기 트랜잭션을 열고, 결과 기록은 별도 트랜잭션으로
 * 남는다. 하나로 묶으면 메일 실패가 이미 성공한 SSE 까지 롤백시켜 재시도 때 푸시가 또 나간다.
 *
 * <p><b>{@code publish()} 의 의미는 전송 경로에 따라 다르다.</b>
 * <ul>
 *   <li><b>LOCAL</b> — 리스너가 그 자리에서 채널을 다 돌린다. 반환 = 채널 실행까지 끝났다는 뜻이고,
 *       재시도가 남은 실패는 예외로 올라와 행이 미발행으로 남는다. <b>재시도 주체는 outbox 다.</b></li>
 *   <li><b>KAFKA</b> — <b>브로커 ACK 까지</b> 기다린다. 채널 실행은 컨슈머에서 비동기로 일어나므로
 *       반환은 "브로커가 받았다" 까지다. <b>재시도 주체는 Kafka 컨슈머다.</b></li>
 * </ul>
 * 그래서 {@code published_at} 은 "모든 채널이 성공" 이 아니라 <b>"전송 계층에 넘겼다"</b> 로 읽어야 한다.
 * 채널별 성패는 {@code notification_delivery} 가 갖고 있다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxDispatcher {

    private final OutboxEventRepository outboxEventRepository;
    private final NotificationEventCodec notificationEventCodec;
    private final OutboxStateWriter outboxStateWriter;
    private final DomainEventPublisher domainEventPublisher;
    private final OutboxTracing outboxTracing;

    public void dispatch(Long outboxId) {
        OutboxEvent row = outboxEventRepository.findById(outboxId).orElse(null);
        if (row == null || row.getPublishedAt() != null) {
            return; // 이미 처리됐다. 중복으로 집히는 것은 정상 상황이라 조용히 넘어간다.
        }

        NotificationMessage message =
                new NotificationMessage(row.getEventId(), notificationEventCodec.toEvent(row));

        // 기록 시점의 trace 를 부모로 삼아 스팬을 연다. 스코프에 올려야 이어지는 Kafka produce 가
        // 자식으로 붙고, 소비 쪽까지 traceparent 헤더로 이어진다.
        Span span = outboxTracing.startDispatchSpan(
                row.getTraceParent(), row.getEventId(), message.type());
        try (Tracer.SpanInScope ignored = outboxTracing.withSpan(span)) {
            domainEventPublisher.publish(message);
            outboxStateWriter.markPublished(outboxId);
        } catch (RuntimeException e) {
            span.error(e);
            log.warn("outbox publish failed, will retry - eventId={}, cause={}",
                    row.getEventId(), e.toString());
            outboxStateWriter.markRetryLater(outboxId, String.valueOf(e));
        } finally {
            span.end();
        }
    }

    /** 릴레이가 예상 못 한 실패를 만났을 때(행 조회 실패 등) 다음 주기로 미룬다. */
    public void recordFailure(Long outboxId, Throwable cause) {
        outboxStateWriter.markRetryLater(outboxId, String.valueOf(cause));
    }
}
