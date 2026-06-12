package net.watchbox.domain.notification.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * memberId → SseEmitter 매핑 저장소 (단일 서버 in-memory).
 *
 * <p>멀티 서버로 확장 시: Redis Pub/Sub 으로 노드 간 메시지 전파하는 구조로 교체 필요.
 * 현 구현은 ConcurrentHashMap 으로 동시성만 보장.
 *
 * <p>한 사용자가 여러 디바이스/탭으로 접속할 수 있다면 Map<Long, List<SseEmitter>> 로 확장.
 * 현재는 1:1 (마지막 연결만 유지).
 */
@Slf4j
@Repository
public class SseEmitterRepository {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter save(Long memberId, SseEmitter emitter) {
        emitters.put(memberId, emitter);
        log.debug("SSE emitter saved. memberId={}, total={}", memberId, emitters.size());
        return emitter;
    }

    public Optional<SseEmitter> findByMemberId(Long memberId) {
        return Optional.ofNullable(emitters.get(memberId));
    }

    public void deleteByMemberId(Long memberId) {
        SseEmitter removed = emitters.remove(memberId);
        if (removed != null) {
            log.debug("SSE emitter removed. memberId={}, total={}", memberId, emitters.size());
        }
    }

    public int size() {
        return emitters.size();
    }

    /** 전체 emitter 순회용 (heartbeat 브로드캐스트 등). ConcurrentHashMap이라 순회 중 삭제 안전. */
    public Set<Map.Entry<Long, SseEmitter>> entries() {
        return emitters.entrySet();
    }
}
