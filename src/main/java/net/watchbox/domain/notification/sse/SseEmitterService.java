package net.watchbox.domain.notification.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.notification.dto.response.NotificationResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * SSE 연결/전송 관리.
 *
 * <ul>
 *   <li>{@link #subscribe} - 클라이언트 SSE 연결 생성</li>
 *   <li>{@link #send} - 특정 사용자에게 알림 push (오프라인이면 no-op)</li>
 *   <li>연결 종료/타임아웃/에러 시 emitter 자동 정리</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(SseProperties.class)
public class SseEmitterService {

    private static final String CONNECT_EVENT = "connect";
    private static final String NOTIFICATION_EVENT = "notification";

    private final SseEmitterRepository emitterRepository;
    private final SseProperties sseProperties;

    /**
     * SSE 구독 시작. 연결 직후 'connect' 이벤트로 핸드셰이크 (프록시 buffer flush + 클라이언트 연결 확인).
     */
    public SseEmitter subscribe(Long memberId) {
        long timeoutMillis = sseProperties.timeout().toMillis();
        SseEmitter emitter = new SseEmitter(timeoutMillis);

        emitter.onCompletion(() -> {
            log.debug("SSE completed. memberId={}", memberId);
            emitterRepository.deleteByMemberId(memberId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE timeout. memberId={}", memberId);
            emitter.complete();
        });
        emitter.onError(e -> {
            log.debug("SSE error. memberId={}, msg={}", memberId, e.getMessage());
            emitter.complete();
        });

        emitterRepository.save(memberId, emitter);

        // 핸드셰이크 - 빈 데이터 한 번 보내야 프록시 buffer flush + 클라이언트 onopen 트리거됨
        try {
            emitter.send(SseEmitter.event()
                    .name(CONNECT_EVENT)
                    .data("connected"));
        } catch (IOException e) {
            log.warn("SSE handshake failed. memberId={}", memberId, e);
            emitter.complete();
        }

        return emitter;
    }

    /**
     * 특정 사용자에게 알림 push.
     * 오프라인(emitter 없음)이면 no-op — DB 영속은 호출 측 책임.
     */
    public void send(Long receiverId, NotificationResponse notification) {
        emitterRepository.findByMemberId(receiverId).ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(notification.notificationId()))
                        .name(NOTIFICATION_EVENT)
                        .data(notification));
            } catch (IOException e) {
                log.warn("SSE send failed. receiverId={}, notificationId={}",
                        receiverId, notification.notificationId(), e);
                emitter.complete();
                emitterRepository.deleteByMemberId(receiverId);
            }
        });
    }
}
