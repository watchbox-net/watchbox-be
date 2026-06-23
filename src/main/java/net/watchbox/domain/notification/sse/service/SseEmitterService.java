package net.watchbox.domain.notification.sse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.notification.dto.response.NotificationResponse;
import net.watchbox.domain.notification.sse.repository.SseEmitterRepository;
import net.watchbox.global.properties.SseProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

    /**
     * Heartbeat(keep-alive) 브로드캐스트.
     *
     * <p>주기적으로 SSE 주석(comment) 라인을 보내 idle 연결이 nginx/프록시의
     * idle timeout(보통 60s)에 끊기는 것을 방지한다. comment 라인은 클라이언트
     * 이벤트를 발생시키지 않아 EventSource는 영향받지 않는다.
     *
     * <p>heartbeat 전송이 실패하면 죽은 연결로 판단해 정리 → 클라이언트는 재연결.
     *
     * <p>주기는 notification.sse.heartbeat(현재 30s)와 동일하게 맞춤.
     */
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void sendHeartbeat() {
        emitterRepository.entries().forEach(entry -> {
            Long memberId = entry.getKey();
            SseEmitter emitter = entry.getValue();
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (Exception e) {
                log.debug("SSE heartbeat failed, cleaning up. memberId={}", memberId);
                emitter.complete();                           // ① 서버 쪽 스트림 닫기
                emitterRepository.deleteByMemberId(memberId); // ② 맵에서 죽은 emitter 제거 (청소)
            }
        });
    }
}
