package net.watchbox.domain.notification.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.dto.response.NotificationResponse;
import net.watchbox.domain.notification.entity.Notification;
import net.watchbox.domain.notification.service.NotificationCommandService;
import net.watchbox.domain.notification.service.NotificationQueryService;
import net.watchbox.domain.notification.sse.service.SseEmitterService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 알림 도메인 진입점.
 *
 * <ul>
 *   <li>SSE 구독 + 미노출 알림 catchup 푸시</li>
 *   <li>스낵바 노출 ACK</li>
 * </ul>
 *
 * <p><b>알림 발송은 여기 없다.</b> 이벤트 수신 → 저장 + 푸시는 채널 단위로 재시도·멱등을
 * 관리해야 해서 {@code SseChannelHandler} 로 옮겼다. 여기는 클라이언트가 직접 부르는
 * 조회·구독 진입점만 남는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService notificationQueryService;
    private final SseEmitterService sseEmitterService;

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
