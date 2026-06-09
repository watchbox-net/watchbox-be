package net.watchbox.domain.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.converter.NotificationPayloadConverter;
import net.watchbox.domain.notification.dto.payload.NotificationPayload;
import net.watchbox.global.entity.BaseTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class Notification extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    Member receiver;

    @Enumerated(EnumType.STRING)
    NotificationType notificationType;

    @Column(columnDefinition = "json")
    @Convert(converter = NotificationPayloadConverter.class)
    NotificationPayload payload;  // 안에 boxId, contentId 등은 Long 값으로

    @Builder.Default
    boolean isRead = false;

    /**
     * 클라이언트에서 스낵바로 실제 노출되었는지 여부.
     * 클라이언트가 렌더 후 ACK 보내면 true 로 마킹 — 서버의 SSE push 성공이 아니라
     * "사용자에게 보인 시점" 을 기준으로 함 (백그라운드 탭/네트워크 오류 등 부정확성 회피).
     */
    @Builder.Default
    boolean snackbarShown = false;

    public void markSnackbarShown() {
        this.snackbarShown = true;
    }
}
