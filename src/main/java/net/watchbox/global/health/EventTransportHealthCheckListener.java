package net.watchbox.global.health;

import lombok.extern.slf4j.Slf4j;
import net.watchbox.global.event.setting.EventTransportSettings;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 프로브 이벤트 수신자. <b>로컬 경로와 Kafka 경로가 같은 future 레지스트리를 완료</b>시킨다.
 *
 * <p>덕분에 헬스체크 컨트롤러는 어느 경로로 갔는지 몰라도 왕복 성공만 확인하면 된다.
 * 전송 방식을 바꿔도 검증 코드가 그대로라는 점이 이 설계의 확인 포인트이기도 하다.
 *
 * <p>Kafka 리스너는 {@code autoStartup = false} 다. 브로커가 꺼진 상태에서 컨슈머가 살아있으면
 * 무한 재연결로 로그가 폭발하므로, 시작·정지는 {@link EventTransportSettings} 가 제어한다.
 *
 * <p>현재 이 컨슈머가 다루는 타입은 프로브뿐이다. 실제 도메인 이벤트를 태울 때는
 * 여러 타입을 역직렬화해야 하므로 타입 헤더 사용 여부를 먼저 정해야 한다.
 */
@Slf4j
@Component
public class EventTransportHealthCheckListener {

    private final Map<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

    /** 해당 probeId 의 수신을 기다리는 future 등록. */
    public CompletableFuture<String> register(String probeId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        pending.put(probeId, future);
        return future;
    }

    /** 대기 종료 후 정리 (timeout/완료 무관 항상 호출). */
    public void unregister(String probeId) {
        pending.remove(probeId);
    }

    /** LOCAL 경로 수신. */
    @EventListener
    public void onLocal(EventTransportProbe probe) {
        complete(probe);
    }

    /** KAFKA 경로 수신. */
    @KafkaListener(
            id = EventTransportSettings.DOMAIN_EVENT_LISTENER_ID,
            topics = "${kafka.topics.domain-events}",
            autoStartup = "false",
            containerFactory = "domainEventKafkaListenerContainerFactory"
    )
    public void onKafka(EventTransportProbe probe) {
        complete(probe);
    }

    private void complete(EventTransportProbe probe) {
        CompletableFuture<String> future = pending.get(probe.probeId());
        if (future != null) {
            future.complete(probe.value());
        }
        // 매칭 안 되면 무시 (다른 인스턴스가 등록한 probeId 또는 stale 메시지)
    }
}
