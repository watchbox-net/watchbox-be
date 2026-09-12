package net.watchbox.domain.notification.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.watchbox.domain.notification.converter.NotificationPayloadConverter;
import net.watchbox.domain.notification.dto.payload.NotificationPayload;
import net.watchbox.domain.notification.entity.NotificationType;
import net.watchbox.domain.notification.event.NotificationEvent;
import net.watchbox.global.entity.BaseTime;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 발행 대기 중인 알림 이벤트. <b>도메인 트랜잭션과 같은 트랜잭션에 기록된다.</b>
 *
 * <p><b>왜 필요한가(dual write)</b>: 초대 저장(MySQL)과 이벤트 발행은 서로 다른 저장소라
 * 한 트랜잭션으로 묶을 수 없다. 커밋 전에 발행하면 롤백된 초대의 알림이 나가고,
 * 커밋 후에 발행하면 그 사이에 죽었을 때 알림이 영영 사라진다.
 * 이벤트를 <b>같은 DB 의 테이블</b>에 적어두면 도메인 변경과 원자적으로 묶이고,
 * 발행이 실패해도 행이 남아 재시도할 수 있다.
 *
 * <p>{@code publishedAt IS NULL} 하나가 "보낼 게 남았다"는 상태 전부다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "notification_outbox",
        indexes = @Index(name = "idx_outbox_pending", columnList = "published_at, next_attempt_at, outbox_id")
)
public class OutboxEvent extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outboxId;

    /**
     * 이벤트 고유 식별자. 지금은 추적용이고, <b>소비 쪽 중복 제거 키로 쓰일 자리</b>다.
     * (at-least-once 라 같은 행이 두 번 발행될 수 있다 — 재시도 단계에서 다룬다)
     */
    @Column(nullable = false, unique = true, length = 36)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType notificationType;

    /**
     * 수신자 ID 목록을 쉼표로 이어 붙인 값.
     *
     * <p>조회 조건으로 쓸 일이 없고 <b>이벤트 복원에만</b> 필요해서 별도 테이블을 두지 않았다.
     * 수신자로 검색해야 할 일이 생기면 그때 정규화하면 된다.
     */
    @Column(nullable = false, length = 1000)
    private String receiverIds;

    @Convert(converter = NotificationPayloadConverter.class)
    @Column(columnDefinition = "json", nullable = false)
    private NotificationPayload payload;

    /** null 이면 미발행. 이 컬럼 하나가 상태 전부다. */
    private LocalDateTime publishedAt;

    /** 발행 시도 횟수. 재시도 간격과 포기 판단에 쓴다. */
    @Column(nullable = false)
    private int attempt;

    /** 다음 시도 가능 시각(backoff). null 이면 즉시 가능. */
    private LocalDateTime nextAttemptAt;

    /** 마지막 실패 원인. 운영에서 "왜 안 갔는지" 를 DB 로 답할 수 있게 남긴다. */
    @Column(length = 500)
    private String lastError;

    /**
     * 기록 시점의 W3C {@code traceparent}. 발행할 때 이걸 부모로 삼아 <b>trace 를 잇는다.</b>
     *
     * <p>기록(요청 스레드)과 발행(릴레이 스레드)은 시점도 스레드도 달라 스레드 로컬로는 못 넘긴다.
     * outbox 가 이벤트를 나르는 김에 trace 도 같이 나른다.
     */
    @Column(length = 64)
    private String traceParent;

    public static OutboxEvent from(NotificationEvent event) {
        return from(event, null);
    }

    public static OutboxEvent from(NotificationEvent event, String traceParent) {
        OutboxEvent row = new OutboxEvent();
        row.traceParent = traceParent;
        row.eventId = UUID.randomUUID().toString();
        row.notificationType = event.notificationType();
        row.receiverIds = join(event.receiverIds());
        row.payload = event.payload();
        return row;
    }

    public List<Long> receiverIdList() {
        if (receiverIds == null || receiverIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(receiverIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
    }

    public void markPublished(LocalDateTime now) {
        this.publishedAt = now;
        this.nextAttemptAt = null;
        this.lastError = null;
    }

    /** 실패를 기록하고 다음 시도 시각을 미룬다. 행은 미발행으로 남아 다시 집힌다. */
    public void markFailed(LocalDateTime nextAttemptAt, String error) {
        this.attempt++;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = error == null ? null : error.substring(0, Math.min(error.length(), 500));
    }

    private static String join(List<Long> ids) {
        return ids.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
    }
}
