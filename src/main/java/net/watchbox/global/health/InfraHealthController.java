package net.watchbox.global.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.global.properties.KafkaTopicProperties;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.watchbox.global.health.HealthStatus.CONNECTED;
import static net.watchbox.global.health.HealthStatus.DISCONNECTED;

@RestController
@RequestMapping("/health/infra")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "HealthCheck - Infra", description = "인프라(DB, Redis, Kafka 등) 연결 확인")
public class InfraHealthController {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final StringRedisTemplate stringRedisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicProperties topics;

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
            description = "헬스체크 토픽에 메시지 produce → consume 으로 일치 확인. Redis R/W 와 같은 강한 검증.")
    @GetMapping("/kafka/pub-sub")
    public ResponseEntity<Map<String, Object>> kafkaPubSubCheck() {
        Map<String, Object> body = new LinkedHashMap<>();
        String key = "hc-" + UUID.randomUUID();
        String expected = "ok-" + System.currentTimeMillis();
        String topic = topics.healthCheck();

        try {
            // 1. Produce
            SendResult<String, String> sendResult = kafkaTemplate
                    .send(topic, key, expected)
                    .get(3, TimeUnit.SECONDS);

            long offset = sendResult.getRecordMetadata().offset();
            int partition = sendResult.getRecordMetadata().partition();

            // 2. Consume — 일회용 consumer 로 발행된 offset 지점만 polling
            String actual = consumeOne(topic, partition, offset, key);

            // producer 가 JsonSerializer 라 String 이 "..." 로 감싸짐 → StringDeserializer 로 읽으면 따옴표 포함
            // 양쪽 끝 따옴표를 제거해 정규화 후 비교
            boolean ok = expected.equals(stripJsonQuotes(actual));
            body.put("status", ok ? CONNECTED : DISCONNECTED);
            body.put("topic", topic);
            body.put("partition", partition);
            body.put("offset", offset);
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
        }
    }

    /** JsonSerializer 가 String 을 감싼 양끝 따옴표 제거 ("ok-123" -> ok-123). */
    private String stripJsonQuotes(String value) {
        if (value == null) return null;
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private Properties adminProps() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 5000);
        return props;
    }

    /** 발행한 메시지를 해당 partition/offset 에서 단건 소비하여 값 반환. 없으면 null. */
    private String consumeOne(String topic, int partition, long offset, String key) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 일회용 group — 다른 컨슈머 offset 에 영향 X
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "hc-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            TopicPartition tp = new TopicPartition(topic, partition);
            consumer.assign(List.of(tp));
            consumer.seek(tp, offset);

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(3));
            for (ConsumerRecord<String, String> r : records) {
                if (key.equals(r.key())) return r.value();
            }
            return null;
        }
    }
}
