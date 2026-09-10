package net.watchbox.domain.notification.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.global.properties.OutboxProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * outbox 에 쌓인 이벤트를 꺼내 발행한다. <b>트랜잭션 밖에서, 커밋 이후에만</b> 돈다.
 *
 * <p>깨우는 경로가 둘이다.
 * <ul>
 *   <li><b>커밋 직후 트리거</b> — 방금 기록한 알림이 폴링 주기만큼 늦어지지 않게</li>
 *   <li><b>주기 폴링</b> — 트리거가 유실되거나(프로세스 종료 등) 재시도가 남아 있을 때의 백스톱.
 *       <b>이쪽이 보장을 담당한다</b>. 트리거는 지연을 줄일 뿐이라 실패해도 손해가 없다.</li>
 * </ul>
 *
 * <p><b>스케줄러 스레드를 잡지 않는다</b>: 기본 {@code TaskScheduler} 는 스레드가 1개라
 * 여기서 SMTP 를 기다리면 SSE 하트비트·캐시 워밍 같은 다른 {@code @Scheduled} 가 밀린다.
 * 가상 스레드로 넘기고, 두 경로가 동시에 들어와도 <b>한 번에 하나만</b> 돌게 막는다
 * (같은 행을 두 스레드가 집으면 중복 발행이 된다).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    /** 한 번에 가져올 행 수. 실패 시 재시도 단위이기도 하다. */
    private static final int BATCH_SIZE = 100;

    /** 한 번 깨어났을 때 처리할 최대 배치 수. 무한 루프를 막는 안전장치. */
    private static final int MAX_BATCHES_PER_RUN = 10;

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxDispatcher outboxDispatcher;
    private final OutboxProperties outboxProperties;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(fixedDelayString = "${notification.outbox.poll-interval}")
    public void pollPeriodically() {
        wakeUp("poll");
    }

    /** 기록 트랜잭션이 커밋된 뒤에 깨운다. 커밋 전에 깨우면 아직 안 보이는 행을 찾게 된다. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxAppended(OutboxAppended event) {
        wakeUp("append");
    }

    private void wakeUp(String trigger) {
        if (!running.compareAndSet(false, true)) {
            return; // 이미 돌고 있다. 남은 행은 그 실행이 가져간다.
        }
        Thread.ofVirtual().name("outbox-relay-" + trigger).start(() -> {
            try {
                drain();
            } catch (Exception e) {
                // 여기서 죽어도 다음 폴링이 다시 시도한다.
                log.warn("outbox relay run failed - trigger={}, cause={}", trigger, e.toString());
            } finally {
                running.set(false);
            }
        });
    }

    void drain() {
        for (int batch = 0; batch < MAX_BATCHES_PER_RUN; batch++) {
            // maxAttempt 는 채널 상한보다 큰 백스톱이다. 정상적으로는 채널이 먼저 종결돼
            // 행이 닫히므로, 여기 걸리는 행은 조율 자체가 실패하고 있다는 신호다.
            List<OutboxEvent> pending = outboxEventRepository.findPending(
                    LocalDateTime.now(), outboxProperties.maxAttempt(), PageRequest.of(0, BATCH_SIZE));
            if (pending.isEmpty()) {
                return;
            }
            for (OutboxEvent row : pending) {
                dispatchOne(row.getId(), row.getEventId());
            }
            if (pending.size() < BATCH_SIZE) {
                return; // 마지막 배치였다
            }
        }
    }

    private void dispatchOne(Long outboxId, String eventId) {
        try {
            outboxDispatcher.dispatch(outboxId);
        } catch (Exception e) {
            // 발행 트랜잭션은 롤백됐다. 실패를 별도 트랜잭션으로 남겨 다음 주기에 재시도한다.
            log.warn("outbox dispatch failed, will retry - eventId={}, cause={}", eventId, e.toString());
            outboxDispatcher.recordFailure(outboxId, e);
        }
    }
}
