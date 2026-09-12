package net.watchbox.domain.notification.outbox;

import net.watchbox.domain.notification.dto.payload.BoxInvitationPayload;
import net.watchbox.domain.notification.event.BoxInvitationReceivedEvent;
import net.watchbox.domain.notification.event.NotificationMessage;
import net.watchbox.global.event.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 디스패처의 계약은 <b>"넘기지 못했으면 행을 닫지 않는다"</b> 는 것이다.
 * 넘겼는데 닫지 않으면 중복 발행이 되고, 못 넘겼는데 닫으면 알림이 사라진다.
 */
class OutboxDispatcherTest {

    private static final Long OUTBOX_ID = 1L;
    private static final String EVENT_ID = "evt-1";

    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final NotificationEventCodec codec = mock(NotificationEventCodec.class);
    private final OutboxStateWriter stateWriter = mock(OutboxStateWriter.class);
    private final DomainEventPublisher publisher = mock(DomainEventPublisher.class);

    private final OutboxDispatcher dispatcher =
            new OutboxDispatcher(outboxEventRepository, codec, stateWriter, publisher);

    private final BoxInvitationReceivedEvent event = new BoxInvitationReceivedEvent(
            7L, new BoxInvitationPayload(1L, 10L, "주말 영화", 2L, "현", null));

    @BeforeEach
    void setUp() {
        OutboxEvent row = mock(OutboxEvent.class);
        when(row.getEventId()).thenReturn(EVENT_ID);
        when(row.getPublishedAt()).thenReturn(null);
        when(outboxEventRepository.findById(OUTBOX_ID)).thenReturn(Optional.of(row));
        when(codec.toEvent(row)).thenReturn(event);
    }

    @Test
    @DisplayName("전송 계층에 넘기면 행을 닫는다")
    void closesRowAfterPublish() {
        dispatcher.dispatch(OUTBOX_ID);

        verify(publisher).publish(any(NotificationMessage.class));
        verify(stateWriter).markPublished(OUTBOX_ID);
        verify(stateWriter, never()).markRetryLater(any(), any());
    }

    @Test
    @DisplayName("봉투에 eventId 를 실어 보낸다 — 소비 쪽 멱등 키다")
    void carriesEventIdInEnvelope() {
        dispatcher.dispatch(OUTBOX_ID);

        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().eventId()).isEqualTo(EVENT_ID);
        assertThat(captor.getValue().event()).isEqualTo(event);
    }

    @Test
    @DisplayName("넘기지 못하면 행을 닫지 않고 다음 주기로 미룬다")
    void keepsRowPendingWhenPublishFails() {
        doThrow(new RuntimeException("broker down")).when(publisher).publish(any());

        dispatcher.dispatch(OUTBOX_ID);

        verify(stateWriter).markRetryLater(eq(OUTBOX_ID), any());
        verify(stateWriter, never()).markPublished(any());
    }

    @Test
    @DisplayName("이미 닫힌 행은 다시 발행하지 않는다")
    void ignoresAlreadyPublishedRow() {
        OutboxEvent published = mock(OutboxEvent.class);
        when(published.getPublishedAt()).thenReturn(LocalDateTime.now());
        when(outboxEventRepository.findById(OUTBOX_ID)).thenReturn(Optional.of(published));

        dispatcher.dispatch(OUTBOX_ID);

        verifyNoInteractions(publisher, stateWriter);
    }
}
