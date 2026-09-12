package net.watchbox.domain.notification.delivery;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.watchbox.domain.notification.entity.NotificationChannel;
import net.watchbox.global.entity.BaseTime;

import java.time.LocalDateTime;

/**
 * "이 이벤트를 이 채널로 보냈는가" 를 채널마다 기록한다.
 *
 * <p><b>왜 필요한가</b>: outbox 재시도는 행 단위라, 메일 하나가 실패하면 이미 성공한 SSE 푸시까지
 * 다시 나간다. 리스너 실행 순서도 보장되지 않아 <b>메일이 두 번 발송될 수 있다</b> — 메일은 취소가 안 된다.
 * 채널별로 결과를 남겨두고 이미 끝난 채널은 건너뛴다.
 *
 * <p><b>{@code (event_id, channel)} 유니크 제약이 곧 멱등성</b>이다. 코드가 실수해도 DB 가 막는다.
 *
 * <p><b>남는 한계</b>: 발송에 성공하고 이 행을 쓰기 <b>직전</b>에 죽으면 또 보낸다.
 * <i>발송</i>과 <i>기록</i> 사이가 또 dual write 라, at-least-once 에서 중복은 완전히 못 없앤다.
 * 창을 좁힐 뿐이다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "notification_delivery",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_delivery_event_channel", columnNames = {"event_id", "channel"})
)
public class NotificationDelivery extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryId;

    /** {@code OutboxEvent.eventId}. 이 값을 미리 넣어둔 게 여기서 쓰려던 것이다. */
    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    /** 이 채널의 시도 횟수. outbox 행의 attempt 와 별개다 — 채널마다 상한이 다르다. */
    @Column(nullable = false)
    private int attempt;

    private LocalDateTime sentAt;

    @Column(length = 500)
    private String lastError;

    public static NotificationDelivery of(String eventId, NotificationChannel channel) {
        NotificationDelivery delivery = new NotificationDelivery();
        delivery.eventId = eventId;
        delivery.channel = channel;
        delivery.status = DeliveryStatus.PENDING;
        return delivery;
    }

    public void markSent(LocalDateTime now) {
        this.status = DeliveryStatus.SENT;
        this.sentAt = now;
        this.lastError = null;
    }

    /** 재시도 여지가 있는 실패. 상한에 닿으면 스스로 종결된다. */
    public void markFailed(String error, int maxAttempt) {
        this.attempt++;
        this.lastError = truncate(error);
        this.status = this.attempt >= maxAttempt ? DeliveryStatus.FAILED : DeliveryStatus.PENDING;
    }

    /** 재시도해도 소용없는 실패(주소 없음 등). 상한과 무관하게 바로 종결한다. */
    public void abandon(String error) {
        this.attempt++;
        this.lastError = truncate(error);
        this.status = DeliveryStatus.FAILED;
    }

    private static String truncate(String value) {
        return value == null ? null : value.substring(0, Math.min(value.length(), 500));
    }
}
