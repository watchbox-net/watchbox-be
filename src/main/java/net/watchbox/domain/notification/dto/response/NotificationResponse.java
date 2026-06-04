package net.watchbox.domain.notification.dto.response;

import lombok.Builder;
import net.watchbox.domain.notification.dto.payload.NotificationPayload;
import net.watchbox.domain.notification.entity.Notification;
import net.watchbox.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;

/**
 * 인박스(알림 목록) 조회용 응답 DTO.
 * payload 는 sealed interface 라 Jackson 이 자동으로 type discriminator 포함해 직렬화함.
 */
@Builder
public record NotificationResponse(
        Long notificationId,
        NotificationType type,
        NotificationPayload payload,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .type(n.getNotificationType())
                .payload(n.getPayload())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
