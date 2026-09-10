package net.watchbox.domain.notification.channel;

import net.watchbox.domain.notification.delivery.DeliveryStatus;
import net.watchbox.domain.notification.delivery.NotificationDeliveryRecorder;
import net.watchbox.domain.notification.dto.payload.BoxInvitationPayload;
import net.watchbox.domain.notification.entity.NotificationChannel;
import net.watchbox.domain.notification.event.BoxInvitationReceivedEvent;
import net.watchbox.domain.notification.event.NotificationMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 로컬 경로와 Kafka 컨슈머가 공유하는 규칙이다.
 *
 * <p><b>예외를 던지는지 여부가 곧 재시도 신호</b>라, 그 경계를 테스트로 고정한다.
 * 잘못 던지면 끝난 일을 계속 재시도하고, 안 던지면 실패가 조용히 사라진다.
 */
class NotificationDeliveryExecutorTest {

    private static final String EVENT_ID = "evt-1";

    private final NotificationDeliveryRecorder recorder = mock(NotificationDeliveryRecorder.class);
    private final NotificationDeliveryExecutor executor = new NotificationDeliveryExecutor(recorder);

    private final NotificationChannelHandler handler = mock(NotificationChannelHandler.class);

    private final NotificationMessage message = new NotificationMessage(EVENT_ID,
            new BoxInvitationReceivedEvent(7L,
                    new BoxInvitationPayload(1L, 10L, "주말 영화", 2L, "현", null)));

    private void givenSupportedChannel() {
        when(handler.channel()).thenReturn(NotificationChannel.MAIL);
        when(handler.supports(any())).thenReturn(true);
        when(handler.maxAttempt()).thenReturn(5);
    }

    @Test
    @DisplayName("성공하면 SENT 로 기록한다")
    void marksSentOnSuccess() {
        givenSupportedChannel();

        executor.deliver(handler, message);

        verify(handler).handle(message.event());
        verify(recorder).markSent(EVENT_ID, NotificationChannel.MAIL);
    }

    @Test
    @DisplayName("이미 끝난 채널은 손대지 않는다 — 중복 발송을 막는 지점")
    void skipsTerminalChannel() {
        givenSupportedChannel();
        when(recorder.isTerminal(EVENT_ID, NotificationChannel.MAIL)).thenReturn(true);

        executor.deliver(handler, message);

        verify(handler, never()).handle(any());
        verify(recorder, never()).markSent(any(), any());
    }

    @Test
    @DisplayName("지원하지 않는 이벤트면 조회조차 하지 않는다")
    void skipsUnsupportedEvent() {
        when(handler.supports(any())).thenReturn(false);

        executor.deliver(handler, message);

        verify(handler, never()).handle(any());
        verifyNoInteractions(recorder);
    }

    @Test
    @DisplayName("재시도가 남은 실패는 위로 던져 전송 계층이 다시 시도하게 한다")
    void rethrowsRetryableFailure() {
        givenSupportedChannel();
        RuntimeException cause = new RuntimeException("SMTP down");
        doThrow(cause).when(handler).handle(any());
        when(recorder.markFailed(any(), any(), any(), anyInt())).thenReturn(DeliveryStatus.PENDING);

        assertThatThrownBy(() -> executor.deliver(handler, message)).isSameAs(cause);

        verify(recorder).markFailed(eq(EVENT_ID), eq(NotificationChannel.MAIL), eq(cause), eq(5));
    }

    @Test
    @DisplayName("상한에 닿아 종결되면 던지지 않는다 — 영원히 재시도하지 않기 위해")
    void doesNotRethrowWhenGaveUp() {
        givenSupportedChannel();
        doThrow(new RuntimeException("SMTP down")).when(handler).handle(any());
        when(recorder.markFailed(any(), any(), any(), anyInt())).thenReturn(DeliveryStatus.FAILED);

        assertThatCode(() -> executor.deliver(handler, message)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("재시도해도 소용없는 실패는 즉시 종결하고 던지지 않는다")
    void abandonsNonRetryableFailure() {
        givenSupportedChannel();
        doThrow(new NonRetryableDeliveryException("주소 없음")).when(handler).handle(any());

        assertThatCode(() -> executor.deliver(handler, message)).doesNotThrowAnyException();

        verify(recorder).abandon(eq(EVENT_ID), eq(NotificationChannel.MAIL), any());
        verify(recorder, never()).markFailed(any(), any(), any(), anyInt());
    }
}
