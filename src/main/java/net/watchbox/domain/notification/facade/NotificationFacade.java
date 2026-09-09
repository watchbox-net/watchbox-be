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
import org.springframework.scheduling.annotation.Async;
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
 *   <li>SSE 구독 + 미노출 알림 catchup 푸시</li>
 *   <li>스낵바 노출 ACK</li>
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
    private final NotificationDeliveryMetrics deliveryMetrics;
    private final NotificationFailureInjector failureInjector;

    // ─────────────────── 이벤트 수신 → 알림 발송 ───────────────────

    /**
     * 모든 {@link NotificationEvent} 수신.
     * receiver 수만큼 Notification row 생성 (fan-out on write) + 각 receiver 에 SSE push.
     * 오프라인 사용자는 SSE no-op, DB 저장은 살아있어 다음 구독 시 catchup 으로 푸시됨.
     *
     * <p>{@code @Async("notificationExecutor")} — 발행 도메인 스레드와 분리.
     * 알림 저장/SSE 발송이 발행자 응답속도에 영향 X.
     * JVM 종료 시 큐잉된 이벤트는 유실될 수 있음 (Kafka 도입 전 한계).
     */
    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            for (Long receiverId : event.receiverIds()) {
                failureInjector.maybeFail(NotificationChannel.SSE);
                Notification saved = notificationCommandService.createNotification(receiverId, event.type(), event.payload());
                sseEmitterService.send(receiverId, NotificationResponse.from(saved));
                deliveryMetrics.success(NotificationChannel.SSE);
            }
        } catch (RuntimeException e) {
            // 세기만 하고 그대로 던진다. 여기서 삼키면 "루프 중간에서 터지면 뒤 수신자는 못 받는다" 는
            // 현재의 결함이 가려져 before 측정이 무의미해진다. 이 동작을 고치는 건 Outbox 단계의 몫이다.
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
