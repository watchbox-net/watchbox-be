package net.watchbox.domain.notification.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.dto.response.NotificationResponse;
import net.watchbox.domain.notification.entity.Notification;
import net.watchbox.domain.notification.event.NotificationEvent;
import net.watchbox.domain.notification.service.NotificationCommandService;
import net.watchbox.domain.notification.service.NotificationQueryService;
import net.watchbox.domain.notification.sse.SseEmitterService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 알림 도메인 진입점.
 *
 * <ul>
 *   <li>도메인 이벤트 수신 → Notification 저장 + SSE push</li>
 *   <li>SSE 구독</li>
 *   <li>인박스 조회 / 안읽음 카운트 / 읽음 처리</li>
 * </ul>
 *
 * <p>이벤트 리스너는 {@link TransactionPhase#AFTER_COMMIT} 에서 동작 — 발행 도메인의 트랜잭션이
 * 성공적으로 커밋된 후에만 알림 발송 (정합성 보장).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService notificationQueryService;
    private final SseEmitterService sseEmitterService;

    // ─────────────────── 이벤트 수신 → 알림 발송 ───────────────────

    /**
     * 모든 {@link NotificationEvent} 수신.
     * receiver 수만큼 Notification row 생성 (fan-out on write) + 각 receiver 에 SSE push.
     * 오프라인 사용자는 SSE no-op, DB 저장은 살아있어 인박스에서 확인 가능.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event) {
        for (Long receiverId : event.receiverIds()) {
            Notification saved = notificationCommandService.create(receiverId, event.type(), event.payload());
            sseEmitterService.send(receiverId, NotificationResponse.from(saved));
        }
    }

    // ─────────────────── SSE 구독 ────────────────────

    public SseEmitter subscribe(Member member) {
        return sseEmitterService.subscribe(member.getMemberId());
    }

    // ─────────────────── 조회 ───────────────────

    @Transactional(readOnly = true)
    public List<NotificationResponse> getInbox(Member member) {
        return notificationQueryService.getRecent(member, 50).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Member member) {
        return notificationQueryService.countUnread(member);
    }

    // ─────────────────── 읽음 처리 ───────────────────

    @Transactional
    public void markAsRead(Member member, Long notificationId) {
        notificationCommandService.markAsRead(member, notificationId);
    }

    @Transactional
    public void markAllAsRead(Member member) {
        notificationCommandService.markAllAsRead(member);
    }
}
