package net.watchbox.domain.notification.outbox;

import net.watchbox.domain.notification.channel.NonRetryableDeliveryException;
import net.watchbox.domain.notification.channel.NotificationChannelHandler;
import net.watchbox.domain.notification.delivery.DeliveryStatus;
import net.watchbox.domain.notification.delivery.NotificationDeliveryRecorder;
import net.watchbox.domain.notification.dto.payload.BoxInvitationPayload;
import net.watchbox.domain.notification.entity.NotificationChannel;
import net.watchbox.domain.notification.event.BoxInvitationReceivedEvent;
import net.watchbox.domain.notification.event.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * State 2 의 계약은 <b>"이미 보낸 채널은 다시 보내지 않는다"</b> 는 것이다.
 * 이게 깨지면 재시도할 때마다 SSE 푸시가 중복되고, 최악의 경우 메일이 두 번 나간다.
 */
class OutboxDispatcherTest {

    private static final Long OUTBOX_ID = 1L;
    private static final String EVENT_ID = "evt-1";

    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final NotificationEventCodec codec = mock(NotificationEventCodec.class);
    private final NotificationDeliveryRecorder recorder = mock(NotificationDeliveryRecorder.class);
    private final OutboxStateWriter stateWriter = mock(OutboxStateWriter.class);

    private final NotificationChannelHandler sse = handler(NotificationChannel.SSE, 3);
    private final NotificationChannelHandler mail = handler(NotificationChannel.MAIL, 5);

    private OutboxDispatcher dispatcher;

    private static NotificationChannelHandler handler(NotificationChannel channel, int maxAttempt) {
        NotificationChannelHandler handler = mock(NotificationChannelHandler.class);
        when(handler.channel()).thenReturn(channel);
        when(handler.maxAttempt()).thenReturn(maxAttempt);
        when(handler.supports(any())).thenReturn(true);
        return handler;
    }

    @BeforeEach
    void setUp() {
        dispatcher = new OutboxDispatcher(
                outboxEventRepository, codec, recorder, stateWriter, List.of(sse, mail));

        OutboxEvent row = mock(OutboxEvent.class);
        when(row.getEventId()).thenReturn(EVENT_ID);
        when(row.getPublishedAt()).thenReturn(null);
        when(outboxEventRepository.findById(OUTBOX_ID)).thenReturn(Optional.of(row));

        NotificationEvent event = new BoxInvitationReceivedEvent(
                7L, new BoxInvitationPayload(1L, 10L, "주말 영화", 2L, "현", null));
        when(codec.toEvent(row)).thenReturn(event);
    }

    @Test
    @DisplayName("이미 끝난 채널은 다시 보내지 않는다")
    void skipsTerminalChannel() {
        when(recorder.isTerminal(EVENT_ID, NotificationChannel.SSE)).thenReturn(true);

        dispatcher.dispatch(OUTBOX_ID);

        verify(sse, never()).handle(any());
        verify(mail).handle(any());
    }

    @Test
    @DisplayName("모든 채널이 성공하면 발행 완료로 닫는다")
    void closesRowWhenAllSucceed() {
        dispatcher.dispatch(OUTBOX_ID);

        verify(recorder).markSent(EVENT_ID, NotificationChannel.SSE);
        verify(recorder).markSent(EVENT_ID, NotificationChannel.MAIL);
        verify(stateWriter).markPublished(OUTBOX_ID);
        verify(stateWriter, never()).markRetryLater(any(), any());
    }

    @Test
    @DisplayName("한 채널이 실패해도 다른 채널은 전달된다")
    void oneChannelFailureDoesNotBlockAnother() {
        doThrow(new RuntimeException("SMTP down")).when(mail).handle(any());
        when(recorder.markFailed(eq(EVENT_ID), eq(NotificationChannel.MAIL), any(), anyInt()))
                .thenReturn(DeliveryStatus.PENDING);

        dispatcher.dispatch(OUTBOX_ID);

        verify(recorder).markSent(EVENT_ID, NotificationChannel.SSE);   // ← SSE 는 성공 기록
        verify(recorder).markFailed(eq(EVENT_ID), eq(NotificationChannel.MAIL), any(), eq(5));
    }

    @Test
    @DisplayName("재시도 대기 채널이 남으면 행을 미발행으로 남긴다")
    void keepsRowPendingWhenChannelWillRetry() {
        doThrow(new RuntimeException("SMTP down")).when(mail).handle(any());
        when(recorder.markFailed(any(), any(), any(), anyInt())).thenReturn(DeliveryStatus.PENDING);

        dispatcher.dispatch(OUTBOX_ID);

        verify(stateWriter).markRetryLater(eq(OUTBOX_ID), any());
        verify(stateWriter, never()).markPublished(any());
    }

    @Test
    @DisplayName("상한에 닿아 종결된 채널만 남으면 행을 닫는다 — 영원히 재시도하지 않는다")
    void closesRowWhenRemainingChannelGaveUp() {
        doThrow(new RuntimeException("SMTP down")).when(mail).handle(any());
        when(recorder.markFailed(any(), any(), any(), anyInt())).thenReturn(DeliveryStatus.FAILED);

        dispatcher.dispatch(OUTBOX_ID);

        verify(stateWriter).markPublished(OUTBOX_ID);
    }

    @Test
    @DisplayName("재시도해도 소용없는 실패는 상한과 무관하게 바로 종결한다")
    void abandonsNonRetryableFailure() {
        doThrow(new NonRetryableDeliveryException("주소 없음")).when(mail).handle(any());

        dispatcher.dispatch(OUTBOX_ID);

        verify(recorder).abandon(eq(EVENT_ID), eq(NotificationChannel.MAIL), any());
        verify(recorder, never()).markFailed(any(), any(), any(), anyInt());
        verify(stateWriter).markPublished(OUTBOX_ID);
    }

    @Test
    @DisplayName("지원하지 않는 채널은 호출하지 않는다")
    void skipsUnsupportedChannel() {
        when(mail.supports(any())).thenReturn(false);

        dispatcher.dispatch(OUTBOX_ID);

        verify(mail, never()).handle(any());
        verify(sse).handle(any());
        verify(stateWriter).markPublished(OUTBOX_ID);
    }

    @Test
    @DisplayName("이미 발행된 행은 아무것도 하지 않는다")
    void ignoresAlreadyPublishedRow() {
        OutboxEvent published = mock(OutboxEvent.class);
        when(published.getPublishedAt()).thenReturn(java.time.LocalDateTime.now());
        when(outboxEventRepository.findById(OUTBOX_ID)).thenReturn(Optional.of(published));

        dispatcher.dispatch(OUTBOX_ID);

        verifyNoInteractions(sse, mail, recorder, stateWriter);
    }
}
