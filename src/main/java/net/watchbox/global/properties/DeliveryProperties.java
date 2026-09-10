package net.watchbox.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 채널별 재시도 상한.
 * application.yml:
 * <pre>
 * notification:
 *   delivery:
 *     sse-max-attempt: 5
 *     mail-max-attempt: 10
 * </pre>
 *
 * <p>채널마다 다른 이유는 <b>만회 경로가 다르기</b> 때문이다. SSE 는 실패해도 알림함 + catchup 이
 * 만회하지만, 메일은 못 보내면 그걸로 끝이라 더 오래 붙든다.
 *
 * <p>상수가 아니라 설정인 이유: 재시도 창을 환경마다 다르게 두기 위해서다.
 * 개발·시연 중에는 넉넉해야 실패를 만들고 되돌릴 시간이 있고,
 * 운영에서는 외부 장애가 길어지는 것을 고려해야 한다.
 */
@ConfigurationProperties(prefix = "notification.delivery")
public record DeliveryProperties(
        int sseMaxAttempt,
        int mailMaxAttempt
) {
}
