package net.watchbox.domain.notification.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * 발행 대기 행. 오래된 것부터 처리해 <b>발생 순서를 유지</b>한다.
     *
     * <p>{@code attempt < maxAttempt} 로 걸러 계속 실패하는 행이 배치를 막지 않게 한다.
     * 걸러진 행은 사라지지 않고 미발행으로 남으므로 쿼리 한 번이면 확인된다.
     */
    @Query("""
            select o from OutboxEvent o
            where o.publishedAt is null
              and o.attempt < :maxAttempt
              and (o.nextAttemptAt is null or o.nextAttemptAt <= :now)
            order by o.outboxId
            """)
    List<OutboxEvent> findPending(@Param("now") LocalDateTime now,
                                  @Param("maxAttempt") int maxAttempt,
                                  Pageable pageable);

    /** 적체량. 파이프라인이 막혔는지 보는 지표. */
    long countByPublishedAtIsNull();

    /** 재시도 상한에 걸려 사람이 봐야 하는 행. */
    long countByPublishedAtIsNullAndAttemptGreaterThanEqual(int maxAttempt);
}
