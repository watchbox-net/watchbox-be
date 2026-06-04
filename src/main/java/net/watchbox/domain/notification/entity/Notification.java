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

    public void markAsRead() {
        this.isRead = true;
    }
}
