package net.watchbox.domain.notification.event;

import net.watchbox.domain.notification.dto.payload.NotificationPayload;
import net.watchbox.domain.notification.entity.NotificationType;
import net.watchbox.global.event.DomainEvent;

import java.util.List;

/**
 * 도메인 이벤트 - 알림 트리거.
 *
 * <p><b>발행은 outbox 를 거친다.</b> 각 도메인 facade 가 {@code OutboxRecorder.record(...)} 로
 * 도메인 트랜잭션 <b>안에서</b> 기록하고, {@code OutboxRelay} 가 트랜잭션 밖에서 꺼내
 * {@code DomainEventPublisher} 로 발행한다. 리스너는 그때 호출된다.
 *
 * <p>{@link DomainEvent} 를 상속하는 이유는 <b>전송 경로(로컬/Kafka)를 바꿔도 발행부가
 * 그대로여야</b> 하기 때문이다. {@code type()} 과 {@code partitionKey()} 는 알림 정보에서
 * 파생되므로 각 이벤트가 따로 구현할 필요가 없다.
 *
 * <p>새 이벤트 타입 추가 시: permits 갱신 + NotificationType + NotificationPayload +
 * {@code NotificationEventCodec} 의 복원 분기를 같이 추가한다.
 */
public sealed interface NotificationEvent extends DomainEvent
        permits BoxInvitationReceivedEvent, BoxInvitationRespondedEvent, ContentBoxAddedEvent {

    /** 알림 수신자 ID 목록 (1명이면 size 1). */
    List<Long> receiverIds();

    /**
     * 알림 타입.
     *
     * <p>{@link DomainEvent#type()} 과 이름이 겹쳐 {@code notificationType} 으로 둔다
     * (반환 타입이 달라 같은 이름으로는 둘 다 구현할 수 없다).
     */
    NotificationType notificationType();

    /** 직렬화 가능한 페이로드. 모든 수신자에게 동일한 payload 가 발송됨. */
    NotificationPayload payload();

    @Override
    default String type() {
        return notificationType().name();
    }

    /**
     * 수신자 단위 순서 보장을 노린 키.
     *
     * <p><b>수신자가 여럿인 이벤트는 첫 수신자 기준이라 나머지는 순서가 보장되지 않는다.</b>
     * 제대로 하려면 수신자별로 메시지를 쪼개야 하는데, 그건 채널별 소비를 나누는 단계
     * (Kafka 컨슈머 그룹 분리)에서 같이 다룰 일이다. 로컬 경로에서는 쓰이지 않는다.
     */
    @Override
    default String partitionKey() {
        List<Long> receivers = receiverIds();
        return receivers.isEmpty() ? notificationType().name() : String.valueOf(receivers.getFirst());
    }
}
