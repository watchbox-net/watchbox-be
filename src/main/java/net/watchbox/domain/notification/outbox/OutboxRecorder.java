package net.watchbox.domain.notification.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.notification.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 알림 이벤트를 outbox 에 기록한다. <b>도메인 facade 가 트랜잭션 안에서 호출한다.</b>
 *
 * <p>여기서는 <b>발행하지 않는다</b>. 기록만 하고, 실제 발행은 커밋 이후 {@link OutboxRelay} 가 한다.
 * 이렇게 해야 도메인 변경과 이벤트가 한 트랜잭션으로 묶여 유령 이벤트도 유실도 생기지 않는다.
 *
 * <p>별도 트랜잭션을 열지 않는 것이 요점이다 — 호출한 도메인 트랜잭션에 그대로 참여해야 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxRecorder {

    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OutboxTracing outboxTracing;

    public void record(NotificationEvent event) {
        if (event.receiverIds().isEmpty()) {
            // 받을 사람이 없는 이벤트다(예: 혼자 쓰는 박스). 기록해봐야 발행 시 아무 일도 안 한다.
            log.debug("outbox skipped, no receivers - type={}", event.type());
            return;
        }

        // 지금 trace 를 행에 함께 적어둔다 — 발행은 다른 스레드·다른 시점이라 이것 없이는 끊긴다.
        OutboxEvent row = outboxEventRepository.save(
                OutboxEvent.from(event, outboxTracing.capture()));

        // 커밋 후 릴레이를 깨워 폴링 주기만큼의 지연을 없앤다. 유실돼도 폴링이 받쳐준다.
        applicationEventPublisher.publishEvent(OutboxAppended.INSTANCE);

        log.debug("outbox recorded - eventId={}, type={}, receivers={}",
                row.getEventId(), event.type(), event.receiverIds().size());
    }
}
