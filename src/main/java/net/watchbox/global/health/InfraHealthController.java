package net.watchbox.global.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.global.event.DomainEventPublisher;
import net.watchbox.global.event.setting.EventTransportSettings;
import net.watchbox.global.properties.KafkaTopicProperties;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static net.watchbox.global.health.HealthStatus.CONNECTED;
import static net.watchbox.global.health.HealthStatus.DISCONNECTED;

@RestController
@RequestMapping("/health/infra")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "9-6 [Health] Infra")
public class InfraHealthController {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final StringRedisTemplate stringRedisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicProperties topics;
    private final KafkaHealthCheckListener kafkaHealthCheckListener;
    private final DomainEventPublisher domainEventPublisher;
    private final EventTransportSettings eventTransportSettings;
    private final EventTransportHealthCheckListener eventTransportHealthCheckListener;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ─────────────────── MySQL ───────────────────

    @Operation(summary = "MySQL 연결 헬스체크")
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> dbHealthCheck() {
        Map<String, Object> body = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(2); // 2초 timeout
            body.put("status", valid ? CONNECTED : DISCONNECTED);
            body.put("database", conn.getMetaData().getDatabaseProductName());
            body.put("url", conn.getMetaData().getURL());
            body.put("checkedAt", ZonedDateTime.now());
            return valid
                    ? ResponseEntity.ok(body)
                    : ResponseEntity.status(503).body(body);
        } catch (Exception e) {
            log.warn("DB health check failed", e);
            body.put("status", DISCONNECTED);
            body.put("error", e.getMessage());
            body.put("checkedAt", ZonedDateTime.now());
            return ResponseEntity.status(503).body(body);
        }
    }

    // ─────────────────── Redis ───────────────────

    @Operation(summary = "Redis 연결 헬스체크")
    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> redisHealthCheck() {
        Map<String, Object> body = new LinkedHashMap<>();
        try (RedisConnection conn = redisConnectionFactory.getConnection()) {
            String pong = conn.ping();
            boolean up = "PONG".equalsIgnoreCase(pong);
            body.put("status", up ? CONNECTED : DISCONNECTED);
            body.put("ping", pong);
            body.put("checkedAt", ZonedDateTime.now());
            return up
                    ? ResponseEntity.ok(body)
                    : ResponseEntity.status(503).body(body);
        } catch (Exception e) {
            log.warn("Redis health check failed", e);
            body.put("status", DISCONNECTED);
            body.put("error", e.getMessage());
            body.put("checkedAt", ZonedDateTime.now());
            return ResponseEntity.status(503).body(body);
        }
    }

    @Operation(summary = "Redis 읽기/쓰기 동작 헬스체크",
            description = "임시 키에 값 SET → GET 으로 일치 확인 → DEL 로 정리. PING 보다 강한 검증.")
    @GetMapping("/redis/rw")
    public ResponseEntity<Map<String, Object>> redisReadWriteCheck() {
        Map<String, Object> body = new LinkedHashMap<>();
        String key = "healthcheck:rw:" + UUID.randomUUID();
        String expected = "ok-" + System.currentTimeMillis();

        try {
            // SET (10초 TTL - 만료로 자동 정리되더라도 안전)
            stringRedisTemplate.opsForValue().set(key, expected, Duration.ofSeconds(10));

            // GET
            String actual = stringRedisTemplate.opsForValue().get(key);

            // DEL (즉시 정리)
            Boolean deleted = stringRedisTemplate.delete(key);

            boolean ok = expected.equals(actual);
            body.put("status", ok ? CONNECTED : DISCONNECTED);
            body.put("key", key);
            body.put("expected", expected);
            body.put("actual", actual);
            body.put("deleted", deleted);
            body.put("checkedAt", ZonedDateTime.now());
            return ok
                    ? ResponseEntity.ok(body)
                    : ResponseEntity.status(503).body(body);
        } catch (Exception e) {
            log.warn("Redis R/W health check failed", e);
            body.put("status", DISCONNECTED);
            body.put("key", key);
            body.put("error", e.getMessage());
            body.put("checkedAt", ZonedDateTime.now());
            return ResponseEntity.status(503).body(body);
        }
    }

    // ─────────────────── Kafka ───────────────────

    @Operation(summary = "Kafka 연결 헬스체크",
            description = "AdminClient 로 클러스터 메타데이터(brokers, controller) 조회. Redis PING 과 같은 가벼운 체크.")
    @GetMapping("/kafka")
    public ResponseEntity<Map<String, Object>> kafkaHealthCheck() {
        Map<String, Object> body = new LinkedHashMap<>();
        try (AdminClient admin = AdminClient.create(adminProps())) {
            DescribeClusterResult cluster = admin.describeCluster(
                    new DescribeClusterOptions().timeoutMs(3000));

            String clusterId = cluster.clusterId().get(3, TimeUnit.SECONDS);
            int brokerCount = cluster.nodes().get(3, TimeUnit.SECONDS).size();
            Node controller = cluster.controller().get(3, TimeUnit.SECONDS);

            body.put("status", CONNECTED);
            body.put("clusterId", clusterId);
            body.put("brokerCount", brokerCount);
            body.put("controller", controller != null ? controller.idString() : null);
            body.put("bootstrapServers", bootstrapServers);
            body.put("checkedAt", ZonedDateTime.now());
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.warn("Kafka health check failed", e);
            body.put("status", DISCONNECTED);
            body.put("error", e.getMessage());
            body.put("bootstrapServers", bootstrapServers);
            body.put("checkedAt", ZonedDateTime.now());
            return ResponseEntity.status(503).body(body);
        }
    }

    @Operation(summary = "Kafka 발행/구독 동작 헬스체크",
            description = "헬스체크 토픽에 produce → @KafkaListener 로 consume 후 값 일치 확인. " +
                    "실서비스와 동일한 @KafkaListener 경로라 trace 에 producer/consumer span 모두 잡힘.")
    @GetMapping("/kafka/pub-sub")
    public ResponseEntity<Map<String, Object>> kafkaPubSubCheck() {
        Map<String, Object> body = new LinkedHashMap<>();
        String key = "hc-" + UUID.randomUUID();
        String expected = "ok-" + System.currentTimeMillis();
        String topic = topics.healthCheck();

        // 수신 대기 future 등록 (리스너가 같은 key 메시지 받으면 complete)
        CompletableFuture<String> future = kafkaHealthCheckListener.register(key);
        try {
            // Produce (KafkaTemplate → producer span 자동 계측)
            kafkaTemplate.send(topic, key, expected).get(3, TimeUnit.SECONDS);

            // Consume 대기 (@KafkaListener → consumer span 자동 계측)
            String actual = future.get(5, TimeUnit.SECONDS);

            boolean ok = expected.equals(actual);
            body.put("status", ok ? CONNECTED : DISCONNECTED);
            body.put("topic", topic);
            body.put("expected", expected);
            body.put("actual", actual);
            body.put("checkedAt", ZonedDateTime.now());
            return ok
                    ? ResponseEntity.ok(body)
                    : ResponseEntity.status(503).body(body);
        } catch (Exception e) {
            log.warn("Kafka pub/sub health check failed", e);
            body.put("status", DISCONNECTED);
            body.put("topic", topic);
            body.put("error", e.getMessage());
            body.put("checkedAt", ZonedDateTime.now());
            return ResponseEntity.status(503).body(body);
        } finally {
            kafkaHealthCheckListener.unregister(key);
        }
    }

    // ─────────────────── 도메인 이벤트 전송 경로 ───────────────────

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

    private Properties adminProps() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 5000);
        return props;
    }
}
