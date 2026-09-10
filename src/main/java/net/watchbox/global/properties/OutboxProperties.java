package net.watchbox.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 알림 outbox 릴레이 설정.
 * application.yml:
 * <pre>
 * notification:
 *   outbox:
 *     poll-interval: 5s     # 재시도·트리거 유실을 받쳐주는 백스톱 주기
 *     base-backoff: 5s      # 첫 재시도 간격. 실패마다 2배
 *     max-backoff: 60s      # 간격 상한
 *     max-attempt: 20       # 행 단위 상한(백스톱). 채널 상한보다 커야 한다
 * </pre>
 *
 * @param maxAttempt 행을 더 집지 않는 기준. <b>채널 상한보다 커야 한다</b> —
 *                   같거나 작으면 채널이 종결되기 전에 행이 조회에서 빠져
 *                   영원히 미발행으로 남는다
 */
@ConfigurationProperties(prefix = "notification.outbox")
public record OutboxProperties(
        Duration pollInterval,
        Duration baseBackoff,
        Duration maxBackoff,
        int maxAttempt
) {
}
