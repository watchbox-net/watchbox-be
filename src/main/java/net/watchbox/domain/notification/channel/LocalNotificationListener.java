package net.watchbox.domain.notification.channel;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.notification.event.NotificationMessage;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * LOCAL 전송 경로의 소비자. 봉투를 받아 <b>모든 채널을 순회</b>한다.
 *
 * <p>Kafka 경로에서는 컨슈머 그룹이 채널마다 하나씩이라 자연히 나뉘지만, 로컬에는 그런 게 없어
 * 여기서 순회한다. 채널별 멱등·재시도 규칙은 {@link NotificationDeliveryExecutor} 가 갖고 있어
 * 두 경로가 같은 규칙을 쓴다.
 *
 * <p><b>한 채널이 실패해도 나머지를 계속 시도한다.</b> 여기서 바로 던지면 뒤 채널이 이번 회차에
 * 아예 실행되지 않아, 앞 채널이 계속 실패하는 동안 뒤 채널이 볼모가 된다.
 * 다 돌린 뒤 실패를 올려 릴레이가 행을 재시도하게 한다.
 */
@Component
@RequiredArgsConstructor
public class LocalNotificationListener {

    private final List<NotificationChannelHandler> channelHandlers;
    private final NotificationDeliveryExecutor deliveryExecutor;

    @EventListener
    public void onNotificationMessage(NotificationMessage message) {
        RuntimeException firstFailure = null;
        for (NotificationChannelHandler handler : channelHandlers) {
            try {
                deliveryExecutor.deliver(handler, message);
            } catch (RuntimeException e) {
                if (firstFailure == null) {
                    firstFailure = e;
                }
            }
        }
        if (firstFailure != null) {
            throw firstFailure;
        }
    }
}
