package net.watchbox.global.dev.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.notification.dev.NotificationFailureInjector;
import net.watchbox.domain.notification.entity.NotificationChannel;
import net.watchbox.domain.notification.metrics.NotificationDeliveryMetrics;
import net.watchbox.global.event.DomainEventPublisher;
import net.watchbox.global.event.EventTransport;
import net.watchbox.global.event.setting.EventTransportSettings;
import net.watchbox.global.health.EventTransportHealthCheckListener;
import net.watchbox.global.health.EventTransportProbe;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static net.watchbox.global.health.HealthStatus.CONNECTED;
import static net.watchbox.global.health.HealthStatus.DISCONNECTED;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/infra")
@Tag(name = "9-3 [Dev] Infra")
public class DevInfraController {

    private final StringRedisTemplate stringRedisTemplate;
    private final EventTransportSettings eventTransportSettings;
    private final NotificationFailureInjector notificationFailureInjector;
    private final MeterRegistry meterRegistry;
    private final DomainEventPublisher domainEventPublisher;
    private final EventTransportHealthCheckListener eventTransportHealthCheckListener;

    /**
     * 도메인 이벤트 전송 경로를 런타임에 바꾼다. Kafka 브로커는 비용 때문에 평소 꺼두므로,
     * 재배포 없이 필요할 때만 켜서 쓰기 위한 조작이다.
     *
     * <p>DB 에 저장되어 재기동 후에도 유지된다. 전환 시 Kafka 컨슈머도 함께 start/stop 된다.
     *
     * <p><b>DB 를 직접 수정하면 반영되지 않는다.</b> 발행 경로가 메모리 캐시를 읽고,
     * 그 캐시를 갱신하는 것은 이 API 뿐이다(이벤트마다 DB 를 치면 발행 경로가 병목이 된다).
     * 직접 수정했다면 재기동이 필요하다.
     */
    @Operation(summary = "도메인 이벤트 전송 경로 전환 (LOCAL ↔ KAFKA)",
            description = "DB 에 저장되어 재기동 후에도 유지된다. KAFKA 로 전환할 때 브로커가 닿지 않으면 " +
                    "전환하지 않고 409 를 반환한다 — 조용히 LOCAL 로 남으면 '바꿨는데 왜 안 되지'를 디버깅하게 된다. " +
                    "전환 결과 확인은 GET /dev/infra/event-transport.")
    @PostMapping("/event-transport/switch")
    public ResponseEntity<Map<String, Object>> switchEventTransport(@RequestParam EventTransport transport) {
        Map<String, Object> body = new LinkedHashMap<>();
        try {
            body.put("transport", eventTransportSettings.switchTo(transport));
            body.put("switchedAt", ZonedDateTime.now());
            return ResponseEntity.ok(body);
        } catch (IllegalStateException e) {
            log.warn("event transport switch rejected - target={}, cause={}", transport, e.getMessage());
            body.put("transport", eventTransportSettings.current());
            body.put("error", e.getMessage());
            return ResponseEntity.status(409).body(body);
        }
    }

    @Operation(summary = "이벤트 전송 경로 왕복 헬스체크",
            description = "현재 설정된 경로(LOCAL/KAFKA)로 프로브 이벤트를 발행 → 수신 확인. " +
                    "로컬이든 카프카든 같은 리스너 레지스트리로 수렴하므로 검증 방식이 동일하다. " +
                    "configured 는 DB 에 저장된 값, transport 는 실제 동작 중인 값 — " +
                    "기동 시 브로커가 닿지 않아 LOCAL 로 폴백했다면 둘이 다를 수 있다.")
    @GetMapping("/event-transport")
    public ResponseEntity<Map<String, Object>> eventTransportCheck() {
        Map<String, Object> body = new LinkedHashMap<>();
        String probeId = "probe-" + UUID.randomUUID();
        String expected = "ok-" + System.currentTimeMillis();

        CompletableFuture<String> future = eventTransportHealthCheckListener.register(probeId);
        try {
            domainEventPublisher.publish(new EventTransportProbe(probeId, expected));

            String actual = future.get(5, TimeUnit.SECONDS);
            boolean ok = expected.equals(actual);

            body.put("status", ok ? CONNECTED : DISCONNECTED);
            body.put("transport", eventTransportSettings.current());
            body.put("configured", eventTransportSettings.configured());
            body.put("expected", expected);
            body.put("actual", actual);
            body.put("checkedAt", ZonedDateTime.now());
            return ok ? ResponseEntity.ok(body) : ResponseEntity.status(503).body(body);
        } catch (Exception e) {
            log.warn("event transport health check failed", e);
            body.put("status", DISCONNECTED);
            body.put("transport", eventTransportSettings.current());
            body.put("error", e.getMessage());
            body.put("checkedAt", ZonedDateTime.now());
            return ResponseEntity.status(503).body(body);
        } finally {
            eventTransportHealthCheckListener.unregister(probeId);
        }
    }

    /**
     * 사용자가 지정한 key/value/TTL 로 Redis 에 값을 저장하고 즉시 GET 으로 확인.
     * 헬스체크와 달리 <b>삭제하지 않음</b> — RedisInsight 등으로 직접 확인하거나 TTL 만료까지 유지.
     *
     * @param ttlSeconds 만료 시간(초). 0 이하면 만료 없이(persist) 저장.
     */
    @Operation(summary = "Redis 임의 키 저장 (삭제 안 함)",
            description = "입력한 key/value 를 지정 TTL 로 SET 후 GET 으로 확인. 삭제하지 않으므로 TTL 만료 전까지 Redis 에 남음. " +
                    "ttlSeconds 0 이하면 만료 없이 저장.")
    @PostMapping("/redis/set")
    public ResponseEntity<Map<String, Object>> redisSet(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(defaultValue = "0") long ttlSeconds
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        try {
            if (ttlSeconds > 0) {
                stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
            } else {
                stringRedisTemplate.opsForValue().set(key, value); // 만료 없음
            }

            String actual = stringRedisTemplate.opsForValue().get(key);
            Long ttl = stringRedisTemplate.getExpire(key); // 남은 TTL(초). -1 만료없음, -2 없음

            boolean ok = value.equals(actual);
            body.put("ok", ok);
            body.put("key", key);
            body.put("value", value);
            body.put("actual", actual);
            body.put("ttlSeconds", ttl);
            body.put("savedAt", ZonedDateTime.now());
            return ok
                    ? ResponseEntity.ok(body)
                    : ResponseEntity.status(503).body(body);
        } catch (Exception e) {
            log.warn("Redis dev set failed. key={}", key, e);
            body.put("ok", false);
            body.put("key", key);
            body.put("error", e.getMessage());
            body.put("savedAt", ZonedDateTime.now());
            return ResponseEntity.status(503).body(body);
        }
    }

    // ─────────────────── 알림 파이프라인 계측 (Outbox 전후 비교용) ───────────────────

    /**
     * 채널별 실패율을 런타임에 조절한다. <b>도착률을 결정론적으로 재기 위한 장치</b>다.
     *
     * <p>프로세스를 죽여 유실을 재현하면 타이밍이 매번 달라 숫자가 들쭉날쭉하다.
     * 실패율을 고정하면 같은 조건을 반복할 수 있고, Outbox 도입 후
     * <b>"실패율과 무관하게 도착률 100%"</b> 를 숫자로 보일 수 있다.
     *
     * <p>운영 프로파일에서는 거부된다. 측정이 끝나면 <b>반드시 0 으로 되돌려라</b> —
     * 켜둔 걸 잊고 "왜 알림이 안 오지" 를 디버깅하는 사고가 가장 흔하다.
     */
    @Operation(summary = "알림 채널 실패율 주입 (측정용)",
            description = "rate 는 0.0~1.0. 운영 프로파일에서는 409. 측정 후 0 으로 되돌릴 것.")
    @PostMapping("/notification/failure-rate")
    public ResponseEntity<Map<String, Object>> setNotificationFailureRate(
            @RequestParam NotificationChannel channel,
            @RequestParam double rate
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        try {
            notificationFailureInjector.setFailureRate(channel, rate);
            body.put("rates", notificationFailureInjector.currentRates());
            body.put("changedAt", ZonedDateTime.now());
            return ResponseEntity.ok(body);
        } catch (IllegalStateException | IllegalArgumentException e) {
            body.put("rates", notificationFailureInjector.currentRates());
            body.put("error", e.getMessage());
            return ResponseEntity.status(409).body(body);
        }
    }

    /**
     * 채널별 전달 결과 카운터 스냅샷.
     *
     * <p>Micrometer 카운터는 리셋할 수 없다. 측정할 때는 <b>시작·종료 시점에 각각 찍어 차를 본다.</b>
     * 도착률 = (종료 success − 시작 success) / 발송한 초대 수.
     */
    @Operation(summary = "알림 채널별 전달 결과 스냅샷 (측정용)",
            description = "카운터는 리셋 불가. 측정 시작·종료 시점에 찍어 차를 계산한다.")
    @GetMapping("/notification/delivery")
    public ResponseEntity<Map<String, Object>> notificationDeliverySnapshot() {
        Map<String, Map<String, Double>> byChannel = new TreeMap<>();
        for (Counter counter : meterRegistry.find(NotificationDeliveryMetrics.METER_NAME).counters()) {
            String channel = counter.getId().getTag("channel");
            String result = counter.getId().getTag("result");
            byChannel.computeIfAbsent(channel, k -> new TreeMap<>()).put(result, counter.count());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("delivery", byChannel);
        body.put("failureRates", notificationFailureInjector.currentRates());
        body.put("capturedAt", ZonedDateTime.now());
        return ResponseEntity.ok(body);
    }
}