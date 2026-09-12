package net.watchbox.domain.notification.outbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import net.watchbox.global.properties.OutboxProperties;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 릴레이의 핵심 계약은 <b>"실패한 행이 사라지지 않는다"</b> 는 것이다.
 * 예외를 삼키거나 성공으로 처리해버리면 outbox 를 둔 의미가 통째로 없어진다.
 */
class OutboxRelayTest {

    private static final int MAX_ATTEMPT = 20;

    private final OutboxEventRepository repository = mock(OutboxEventRepository.class);
    private final OutboxDispatcher dispatcher = mock(OutboxDispatcher.class);
    private final OutboxRelay relay = new OutboxRelay(repository, dispatcher,
            new OutboxProperties(Duration.ofSeconds(5), Duration.ofSeconds(5),
                    Duration.ofSeconds(60), MAX_ATTEMPT));

    private static OutboxEvent row(Long id) {
        OutboxEvent row = mock(OutboxEvent.class);
        when(row.getOutboxId()).thenReturn(id);
        when(row.getEventId()).thenReturn("evt-" + id);
        return row;
    }

    private void givenPending(List<OutboxEvent> firstBatch) {
        when(repository.findPending(any(LocalDateTime.class), anyInt(), any(Pageable.class)))
                .thenReturn(firstBatch, List.of());
    }

    @Test
    @DisplayName("발행에 성공하면 실패로 기록하지 않는다")
    void successDoesNotRecordFailure() {
        givenPending(List.of(row(1L)));

        relay.drain();

        verify(dispatcher).dispatch(1L);
        verify(dispatcher, never()).recordFailure(any(), any());
    }

    @Test
    @DisplayName("발행이 실패하면 실패로 기록해 다음 주기에 재시도되게 남긴다")
    void failureIsRecordedForRetry() {
        givenPending(List.of(row(1L)));
        RuntimeException cause = new RuntimeException("SMTP down");
        doThrow(cause).when(dispatcher).dispatch(1L);

        relay.drain();

        verify(dispatcher).recordFailure(eq(1L), eq(cause));
    }

    @Test
    @DisplayName("한 건이 실패해도 나머지 행은 계속 처리한다")
    void oneFailureDoesNotBlockTheBatch() {
        givenPending(List.of(row(1L), row(2L), row(3L)));
        doThrow(new RuntimeException("boom")).when(dispatcher).dispatch(2L);

        relay.drain();

        verify(dispatcher).dispatch(1L);
        verify(dispatcher).dispatch(2L);
        verify(dispatcher).dispatch(3L);
        verify(dispatcher, times(1)).recordFailure(any(), any());
    }

    @Test
    @DisplayName("대기 행이 없으면 조회 한 번으로 끝낸다")
    void stopsWhenNothingPending() {
        when(repository.findPending(any(LocalDateTime.class), anyInt(), any(Pageable.class)))
                .thenReturn(List.of());

        relay.drain();

        verify(repository, times(1)).findPending(any(LocalDateTime.class), anyInt(), any(Pageable.class));
        verifyNoInteractions(dispatcher);
    }

    @Test
    @DisplayName("설정된 상한을 조회 조건으로 넘겨 소진된 행이 배치를 막지 않게 한다")
    void queryExcludesExhaustedRows() {
        when(repository.findPending(any(LocalDateTime.class), anyInt(), any(Pageable.class)))
                .thenReturn(List.of());

        relay.drain();

        verify(repository).findPending(any(LocalDateTime.class), eq(MAX_ATTEMPT), any(Pageable.class));
    }
}
