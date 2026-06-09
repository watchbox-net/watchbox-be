package net.watchbox.domain.notification.dto.response;

import lombok.Builder;
import net.watchbox.domain.notification.dto.payload.NotificationPayload;
import net.watchbox.domain.notification.entity.Notification;
import net.watchbox.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO (SSE push / 인박스 조회 공통).
 *
 * <p>payload 는 sealed interface 라 Jackson 이 자동으로 type discriminator 포함해 직렬화함.
 *
 * <p>{@code showSnackbar} 는 서버가 계산해 내림:
 * <ul>
 *   <li>아직 스낵바 노출되지 않았고 ({@code snackbarShown == false})</li>
 *   <li>타입별 freshness 윈도우 안에 있을 때 ({@code type.isSnackbarFresh(createdAt)})</li>
 * </ul>
 * 클라이언트는 이 플래그가 true 면 스낵바 렌더 후 ACK 엔드포인트로 알려야 함
 * ({@code POST /api/notifications/snackbar-shown}).
 */
@Builder
public record NotificationResponse(
        Long notificationId,
        NotificationType type,
        NotificationPayload payload,
        boolean isRead,
        boolean showSnackbar,
        LocalDateTime createdAt
) {
    public static NotificationResponse of(Notification n, boolean showSnackbar) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .type(n.getNotificationType())
                .payload(n.getPayload())
                .isRead(n.isRead())
                .showSnackbar(showSnackbar)
                .createdAt(n.getCreatedAt())
                .build();
    }

    /**
     * 스낵바 발송 정책에 따라 showSnackbar 자동 계산.
     */
    public static NotificationResponse from(Notification n) {
        boolean showSnackbar = !n.isSnackbarShown()
                && n.getNotificationType().isSnackbarFresh(n.getCreatedAt());
        return of(n, showSnackbar);
    }
}
