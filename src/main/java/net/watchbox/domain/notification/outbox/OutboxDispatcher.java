package net.watchbox.domain.notification.outbox;

import lombok.RequiredArgsConstructor;
import net.watchbox.global.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * outbox 행 하나를 발행한다. <b>행마다 독립 트랜잭션</b>이라 한 건이 실패해도 다른 건에 영향이 없다.
 *
 * <p>{@link OutboxRelay} 와 클래스를 나눈 이유는 <b>자기 호출로는 프록시를 안 거쳐</b>
 * {@code @Transactional} 이 무시되기 때문이다. 릴레이는 반복만 하고 트랜잭션 경계는 여기가 갖는다.
 *
 * <p><b>리스너는 이 트랜잭션 안에서 동기로 실행된다.</b> 그래야 실패가 릴레이까지 올라와
 * 재시도로 이어진다. 비동기로 넘겨버리면 실패를 알 방법이 없어 outbox 를 둔 의미가 사라진다.
 * 대신 채널(SSE·메일)이 순차 실행되므로, 채널별 병렬·독립 재시도는 전송 경로를 나누는
 * 단계(Kafka 컨슈머 그룹)에서 다룬다.
 */
@Service
@RequiredArgsConstructor
public class OutboxDispatcher {

    /** 첫 재시도 간격. 실패마다 2배씩 늘린다. */
    private static final Duration BASE_BACKOFF = Duration.ofSeconds(5);
    private static final Duration MAX_BACKOFF = Duration.ofMinutes(10);

    private final OutboxEventRepository outboxEventRepository;
    private final NotificationEventCodec notificationEventCodec;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * @throws RuntimeException 리스너에서 올라온 실패. 호출자가 {@link #recordFailure} 로 기록한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatch(Long outboxId) {
        OutboxEvent row = outboxEventRepository.findById(outboxId).orElse(null);
        if (row == null || row.getPublishedAt() != null) {
            return; // 이미 처리됐다. 중복 집힘은 정상 상황이라 조용히 넘어간다.
        }
        domainEventPublisher.publish(notificationEventCodec.toEvent(row));
        row.markPublished(LocalDateTime.now());
    }

    /**
     * 실패 기록. <b>새 트랜잭션이어야 한다</b> — 발행 트랜잭션이 롤백된 뒤에 남기는 것이라
     * 같은 트랜잭션에 쓰면 기록까지 같이 사라진다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long outboxId, Throwable cause) {
        outboxEventRepository.findById(outboxId).ifPresent(row -> {
            row.markFailed(nextAttemptAt(row.getAttempt()), String.valueOf(cause));
        });
    }

    /** 지수 백오프. 외부 장애가 길어질수록 재시도 간격을 벌려 상대와 우리 둘 다 덜 때린다. */
    private LocalDateTime nextAttemptAt(int currentAttempt) {
        long seconds = BASE_BACKOFF.getSeconds() << Math.min(currentAttempt, 8);
        return LocalDateTime.now().plusSeconds(Math.min(seconds, MAX_BACKOFF.getSeconds()));
    }
}
