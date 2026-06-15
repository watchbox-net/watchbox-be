package net.watchbox.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * SSE 관련 설정.
 * application.yml:
 * <pre>
 * notification:
 *   sse:
 *     timeout: 30m       # SseEmitter timeout (이후 클라이언트가 재연결)
 *     heartbeat: 30s     # heartbeat (keep-alive) 주기
 * </pre>
 */
@ConfigurationProperties(prefix = "notification.sse")
public record SseProperties(
        Duration timeout,
        Duration heartbeat
) {
}
