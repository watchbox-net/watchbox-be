package net.watchbox.domain.notification.channel;

import net.watchbox.domain.notification.entity.NotificationChannel;
import net.watchbox.domain.notification.event.NotificationEvent;

/**
 * 알림 한 건을 <b>한 채널로</b> 전달한다.
 *
 * <p>채널을 인터페이스로 나눈 이유는 <b>실패와 재시도가 채널마다 다르기</b> 때문이다.
 * SSE 는 실패해도 알림함 + catchup 이 만회하지만, 메일은 만회 경로가 없다.
 * 한 덩어리로 묶으면 한쪽 사정에 다른 쪽이 끌려다닌다.
 *
 * <p>어떤 이벤트가 어떤 채널로 가는지는 {@link #supports} 가 결정한다. 현재 정책:
 * <pre>
 *   초대 수신     → SSE + 메일   (응답이 필요하고, 앱 밖에서도 닿아야 한다)
 *   초대 응답     → SSE          (알림함으로 충분)
 *   콘텐츠 추가   → SSE          (전부 메일로 보내면 스팸이 된다)
 * </pre>
 *
 * <p>이 구조가 그대로 <b>Kafka 컨슈머 그룹 하나씩</b>이 된다.
 */
public interface NotificationChannelHandler {

    NotificationChannel channel();

    /** 이 채널이 처리할 이벤트인지. */
    boolean supports(NotificationEvent event);

    /**
     * 전달한다. <b>실패는 예외로 알린다</b> — 삼키면 재시도가 사라진다.
     *
     * @throws NonRetryableDeliveryException 재시도해도 소용없는 실패
     */
    void handle(NotificationEvent event);

    /** 이 채널의 재시도 상한. */
    int maxAttempt();
}
