package net.watchbox.domain.notification.channel;

import net.watchbox.domain.notification.dto.payload.BoxInvitationPayload;
import net.watchbox.domain.notification.event.BoxInvitationReceivedEvent;
import net.watchbox.domain.notification.event.NotificationMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LocalNotificationListenerTest {

    private final NotificationChannelHandler sse = mock(NotificationChannelHandler.class);
    private final NotificationChannelHandler mail = mock(NotificationChannelHandler.class);
    private final NotificationDeliveryExecutor executor = mock(NotificationDeliveryExecutor.class);

    private final LocalNotificationListener listener =
            new LocalNotificationListener(List.of(sse, mail), executor);

    private final NotificationMessage message = new NotificationMessage("evt-1",
            new BoxInvitationReceivedEvent(7L,
                    new BoxInvitationPayload(1L, 10L, "주말 영화", 2L, "현", null)));

    @Test
    @DisplayName("앞 채널이 실패해도 뒤 채널을 계속 시도한다")
    void continuesAfterChannelFailure() {
        doThrow(new RuntimeException("SSE 실패")).when(executor).deliver(eq(sse), any());

        assertThatThrownBy(() -> listener.onNotificationMessage(message))
                .isInstanceOf(RuntimeException.class);

        verify(executor).deliver(eq(mail), any());   // ← 앞 채널 실패에 볼모가 되지 않는다
    }

    @Test
    @DisplayName("실패가 있으면 위로 던져 릴레이가 행을 재시도하게 한다")
    void propagatesFailureToRelay() {
        RuntimeException cause = new RuntimeException("메일 실패");
        doThrow(cause).when(executor).deliver(eq(mail), any());

        assertThatThrownBy(() -> listener.onNotificationMessage(message)).isSameAs(cause);
    }

    @Test
    @DisplayName("모두 성공하면 조용히 끝난다 — 릴레이가 행을 닫는다")
    void silentWhenAllSucceed() {
        listener.onNotificationMessage(message);

        verify(executor).deliver(eq(sse), any());
        verify(executor).deliver(eq(mail), any());
    }
}
