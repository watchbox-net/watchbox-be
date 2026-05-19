package net.watchbox.global.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.watchbox.global.health.HealthStatus.*;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "HealthCheck")
public class HealthCheck {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    @Operation(summary = "서버 헬스체크")
    @GetMapping
    public String healthCheck() {
        return "WatchBox Server " + CONNECTED;
    }

    @Operation(summary = "서버 정보 확인")
    @GetMapping("/info")
    public String info(HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        return String.format(
                "서버 정보 확인 %s - IP: %s - User-Agent: %s - 요청URL: %s",
                ZonedDateTime.now(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                requestUrl
        );
    }

    @Operation(summary = "서버 시간 확인")
    @GetMapping("/time")
    public ZonedDateTime time() {
        return ZonedDateTime.now();
    }

    @Operation(summary = "MySQL 연결 헬스체크")
    @GetMapping("/infra/db")
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

    @Operation(summary = "Redis 연결 헬스체크")
    @GetMapping("/infra/redis")
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
}
