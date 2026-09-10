package net.watchbox.domain.notification.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.dev.NotificationFailureInjector;
import net.watchbox.domain.notification.dto.response.NotificationResponse;
import net.watchbox.domain.notification.entity.Notification;
import net.watchbox.domain.notification.entity.NotificationChannel;
import net.watchbox.domain.notification.metrics.NotificationDeliveryMetrics;
import net.watchbox.domain.notification.event.NotificationEvent;
import net.watchbox.domain.notification.service.NotificationCommandService;
import net.watchbox.domain.notification.service.NotificationQueryService;
import net.watchbox.domain.notification.sse.service.SseEmitterService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 알림 도메인 진입점.
 *
 * <ul>
 *   <li>도메인 이벤트 수신 → Notification 저장 + SSE push</li>
 *   <li>SSE 구독 + 미노출 알림 catchup 푸시</li>
 *   <li>스낵바 노출 ACK</li>
 * </ul>
 *
 * <p>이벤트는 outbox 를 거쳐 들어온다 — 도메인 트랜잭션이 커밋된 뒤에만 발행되므로
 * 롤백된 작업의 알림이 나가지 않고, 발행에 실패하면 릴레이가 재시도한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService notificationQueryService;
    private final SseEmitterService sseEmitterService;
    private final NotificationDeliveryMetrics deliveryMetrics;
    private final NotificationFailureInjector failureInjector;

    // ─────────────────── 이벤트 수신 → 알림 발송 ───────────────────

    /**
     * 모든 {@link NotificationEvent} 수신.
     * receiver 수만큼 Notification row 생성 (fan-out on write) + 각 receiver 에 SSE push.
     * 오프라인 사용자는 SSE no-op, DB 저장은 살아있어 다음 구독 시 catchup 으로 푸시됨.
     *
     * <p><b>평범한 {@code @EventListener} 다.</b> 예전에는 {@code AFTER_COMMIT} + {@code @Async} 로
     * 발행 스레드와 분리했는데, 이제 {@code OutboxRelay} 가 커밋 이후에 트랜잭션 밖 스레드에서
     * 발행하므로 두 장치가 모두 필요 없다. 실패는 삼키지 않고 릴레이로 올려 재시도되게 한다.
     */
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            for (Long receiverId : event.receiverIds()) {
                failureInjector.maybeFail(NotificationChannel.SSE);
                Notification saved = notificationCommandService.createNotification(receiverId, event.notificationType(), event.payload());
                sseEmitterService.send(receiverId, NotificationResponse.from(saved));
                deliveryMetrics.success(NotificationChannel.SSE);
            }
        } catch (RuntimeException e) {
            // 세고 나서 그대로 던진다 — 릴레이가 받아 행을 미발행으로 남기고 재시도한다.
            deliveryMetrics.failure(NotificationChannel.SSE);
            throw e;
        }
    }

    // ─────────────────── SSE 구독 + catchup ───────────────────

    /**
     * SSE 구독 시작 + 오프라인 동안 쌓인 미노출 알림을 즉시 catchup 푸시.
     *
     * <p>흐름:
     * <ol>
     *   <li>{@code sseEmitterService.subscribe} — emitter 등록 + 'connect' 핸드셰이크</li>
     *   <li>{@code snackbarShown=false} 인 최근 알림 최대 5개 조회</li>
     *   <li>각 알림을 {@link NotificationResponse#from} 으로 freshness 검사 (만료 제외)</li>
     *   <li>스낵바 후보들을 같은 emitter 로 즉시 푸시</li>
     * </ol>
     * 클라이언트는 SSE 채널 하나만 들으면 신규 + catchup 모두 받게 됨 (별도 REST 호출 불필요).
     */
    @Transactional(readOnly = true)
    public SseEmitter subscribe(Member member) {
        SseEmitter emitter = sseEmitterService.subscribe(member.getMemberId());

        // catchup — 미노출 알림을 즉시 푸시 (freshness 만료된 것은 NotificationResponse.from 에서 showSnackbar=false 처리)
        List<Notification> candidates = notificationQueryService.getCatchupCandidates(member);
        for (Notification n : candidates) {
            sseEmitterService.send(member.getMemberId(), NotificationResponse.from(n));
        }
        log.debug("SSE subscribed with catchup. memberId={}, catchupCount={}",
                member.getMemberId(), candidates.size());

        return emitter;
    }

    // ─────────────────── 스낵바 노출 ACK ───────────────────

    /**
     * 클라이언트가 알림을 스낵바로 실제 노출했음을 알려옴 — 같은 알림이 다시 스낵바로 뜨지 않게 표시.
     * 본인 소유 알림만 마킹됨.
     */
    @Transactional
    public void markSnackbarShown(Member member, List<Long> notificationIds) {
        notificationCommandService.markSnackbarShown(member, notificationIds);
    }
}
