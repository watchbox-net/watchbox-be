package net.watchbox.domain.notification.kafka;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.notification.channel.MailChannelHandler;
import net.watchbox.domain.notification.channel.NotificationDeliveryExecutor;
import net.watchbox.domain.notification.channel.SseChannelHandler;
import net.watchbox.domain.notification.event.NotificationMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * KAFKA 경로의 소비자. <b>채널마다 컨슈머 그룹을 따로 둔다.</b>
 *
 * <h3>왜 그룹을 나누나 — 이 단계의 핵심</h3>
 * Kafka 는 <b>같은 {@code group.id} 안에서 메시지를 나눠 갖는다.</b> 두 리스너를 한 그룹에 두면
 * 메시지 하나를 둘 중 하나만 받아, <b>절반은 SSE 만 가고 절반은 메일만 가는</b> 상태가 된다.
 * 그룹을 나눠야 <b>양쪽이 모든 메시지를 각자 받는다</b>(fan-out).
 *
 * <p>그룹이 갈리면 <b>오프셋도 갈린다.</b> SMTP 가 막혀 메일 그룹이 같은 오프셋에서 재시도하는
 * 동안에도 <b>SSE 그룹의 오프셋은 계속 전진한다.</b> 예전에 스레드풀을 나눠 얻으려던 격리를,
 * 재시도·지연·관측까지 포함해 전송 계층이 대신해 준다.
 *
 * <p><b>{@code autoStartup = "false"}</b>: 브로커는 비용 때문에 평소 꺼둔다. 컨슈머가 켜져 있으면
 * 무한 재연결로 로그가 폭발하므로, KAFKA 로 전환할 때만 {@code EventTransportSettings} 가 켠다.
 *
 * <p>멱등·재시도 판단은 {@link NotificationDeliveryExecutor} 가 갖는다 — 로컬 경로와 같은 코드다.
 * 여기서는 <b>어느 채널을 맡을지만</b> 정한다.
 */
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    /** 런타임 start/stop 대상 식별자. */
    public static final String SSE_LISTENER_ID = "notification-sse";
    public static final String MAIL_LISTENER_ID = "notification-mail";

    private final NotificationDeliveryExecutor deliveryExecutor;
    private final SseChannelHandler sseChannelHandler;
    private final MailChannelHandler mailChannelHandler;

    @KafkaListener(
            id = SSE_LISTENER_ID,
            topics = "${kafka.topics.notification-events}",
            groupId = "${spring.profiles.active}.notification.sse",
            autoStartup = "false",
            containerFactory = "notificationMessageKafkaListenerContainerFactory"
    )
    public void onSse(NotificationMessage message) {
        deliveryExecutor.deliver(sseChannelHandler, message);
    }

    @KafkaListener(
            id = MAIL_LISTENER_ID,
            topics = "${kafka.topics.notification-events}",
            groupId = "${spring.profiles.active}.notification.mail",
            autoStartup = "false",
            containerFactory = "notificationMessageKafkaListenerContainerFactory"
    )
    public void onMail(NotificationMessage message) {
        deliveryExecutor.deliver(mailChannelHandler, message);
    }
}
