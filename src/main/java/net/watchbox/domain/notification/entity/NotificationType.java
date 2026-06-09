package net.watchbox.domain.notification.entity;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 알림 타입.
 * 새 타입 추가 시 NotificationPayload sealed interface 의 permits 와
 * {@code @JsonSubTypes} 매핑도 함께 추가해야 함.
 *
 * <p>각 타입은 스낵바 노출 freshness 윈도우를 가짐.
 * {@code null} 이면 만료 없음 (액션 필요한 알림은 시간 지나도 노출 필요).
 */
public enum NotificationType {

    /** 공유 박스 초대 받음 — 액션(수락/거절) 필요하므로 만료 없음. */
    BOX_INVITATION_RECEIVED(null),

    /** 공유 박스 초대 결과(수락/거절) — sender 가 알아야 하므로 만료 없음. */
    BOX_INVITATION_RESPONDED(null),

    /** 박스(마이/공유)에 컨텐츠 추가됨 — Ambient 알림이라 5분 지나면 스낵바 불필요. */
    BOX_CONTENT_ADDED(Duration.ofMinutes(5));

    private final Duration snackbarFreshness;

    NotificationType(Duration snackbarFreshness) {
        this.snackbarFreshness = snackbarFreshness;
    }

    /**
     * 알림이 스낵바 노출 가능한 freshness 윈도우 안에 있는지 판정.
     * freshness 가 {@code null} 이면 만료 없음 → 항상 true.
     */
    public boolean isSnackbarFresh(LocalDateTime createdAt) {
        if (snackbarFreshness == null) return true;
        return Duration.between(createdAt, LocalDateTime.now()).compareTo(snackbarFreshness) <= 0;
    }
}
