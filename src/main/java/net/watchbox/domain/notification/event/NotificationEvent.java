package net.watchbox.domain.notification.event;

import net.watchbox.domain.notification.dto.payload.NotificationPayload;
import net.watchbox.domain.notification.entity.NotificationType;

import java.util.List;

/**
 * 도메인 이벤트 - 알림 트리거.
 *
 * <p>각 도메인 facade 에서 발행하고 ({@code ApplicationEventPublisher.publishEvent})
 * NotificationFacade 가 {@code @TransactionalEventListener(AFTER_COMMIT)} 로 수신 → 저장 + SSE push.
 *
 * <p>새 이벤트 타입 추가 시 permits 갱신 + NotificationType + NotificationPayload 같이 추가.
 */
public sealed interface NotificationEvent
        permits BoxInvitationReceivedEvent, BoxContentAddedEvent {

    /** 알림 수신자 ID 목록 (1명이면 size 1). */
    List<Long> receiverIds();

    /** 알림 타입. */
    NotificationType type();

    /** 직렬화 가능한 페이로드. 모든 수신자에게 동일한 payload 가 발송됨. */
    NotificationPayload payload();
}
