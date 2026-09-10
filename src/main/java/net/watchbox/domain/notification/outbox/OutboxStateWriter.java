package net.watchbox.domain.notification.outbox;

import lombok.RequiredArgsConstructor;
import net.watchbox.global.properties.OutboxProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * outbox 행의 상태만 바꾼다.
 *
 * <p>{@link OutboxDispatcher} 에서 분리한 이유는 <b>자기 호출로는 프록시를 안 거쳐</b>
 * {@code @Transactional} 이 무시되기 때문이다. 디스패처는 채널을 도는 조율자라 스스로
 * 트랜잭션을 갖지 않고, 상태 쓰기만 여기로 넘긴다.
 *
 * <p>{@code REQUIRES_NEW} 인 이유는 <b>채널 전달이 롤백돼도 상태 기록은 남아야</b> 하기 때문이다.
 */
@Service
@RequiredArgsConstructor
public class OutboxStateWriter {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxProperties outboxProperties;

    /**
     * 처리 완료로 닫는다.
     *
     * <p>"전부 성공" 이 아니라 <b>"더 할 일이 없음"</b> 이다 — 재시도해도 소용없어 종결된 채널이
     * 섞여 있을 수 있다. 무엇이 실패했는지는 {@code notification_delivery} 가 갖고 있다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublished(Long outboxId) {
        outboxEventRepository.findById(outboxId)
                .ifPresent(row -> row.markPublished(LocalDateTime.now()));
    }

    /** 남은 채널이 있어 나중에 다시 집도록 미룬다. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRetryLater(Long outboxId, String error) {
        outboxEventRepository.findById(outboxId)
                .ifPresent(row -> row.markFailed(nextAttemptAt(row.getAttempt()), error));
    }

    /**
     * 지수 백오프. 외부 장애가 길어질수록 간격을 벌려 상대와 우리 둘 다 덜 때린다.
     *
     * <p>상한을 두는 이유는 <b>장애가 복구된 뒤 반영이 늦어지지 않게</b> 하기 위해서다.
     * 상한이 없으면 몇 번 실패한 건이 수십 분 뒤에나 다시 시도된다.
     */
    private LocalDateTime nextAttemptAt(int currentAttempt) {
        long base = outboxProperties.baseBackoff().getSeconds();
        long seconds = base << Math.min(currentAttempt, 8);   // 시프트 폭을 막아 오버플로 방지
        return LocalDateTime.now().plusSeconds(Math.min(seconds, outboxProperties.maxBackoff().getSeconds()));
    }
}
