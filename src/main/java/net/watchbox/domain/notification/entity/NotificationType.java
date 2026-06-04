package net.watchbox.domain.notification.entity;

/**
 * 알림 타입.
 * 새 타입 추가 시 NotificationPayload sealed interface 의 permits 와
 * @JsonSubTypes 매핑도 함께 추가해야 함.
 */
public enum NotificationType {
    BOX_INVITATION_RECEIVED,       // 공유 박스 초대 받음
    BOX_CONTENT_ADDED              // 박스(마이/공유)에 컨텐츠 추가됨
}
