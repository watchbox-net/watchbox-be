package net.watchbox.domain.notification.event;

import net.watchbox.global.event.DomainEvent;
import net.watchbox.global.event.TopicKey;

/**
 * 전송 계층으로 나가는 알림 봉투. <b>이벤트에 {@code eventId} 를 붙여 나른다.</b>
 *
 * <p><b>왜 봉투인가</b>: 소비 쪽이 "이 이벤트를 이 채널로 이미 보냈는가" 를 판단하려면
 * {@code eventId} 가 필요한데, 이벤트 자체는 도메인 facade 가 만들어서 그 값을 모른다
 * (outbox 기록 시점에 생긴다).
 *
 * <p>Kafka 헤더로 실을 수도 있지만 그러면 <b>로컬 경로에서는 쓸 수 없어 두 전송이 비대칭</b>이 된다.
 * 봉투로 감싸면 LOCAL·KAFKA 어느 쪽이든 <b>같은 객체 모양</b>이라 소비 코드가 하나로 유지된다.
 *
 * <p>라우팅 값은 안에 든 이벤트에서 그대로 가져온다 — 봉투는 운반만 한다.
 */
public record NotificationMessage(
        String eventId,
        NotificationEvent event
) implements DomainEvent {

    @Override
    public String type() {
        return event.type();
    }

    @Override
    public String partitionKey() {
        return event.partitionKey();
    }

    /** 헬스체크 프로브와 타입이 달라 토픽을 나눈다. 섞이면 서로의 메시지에서 역직렬화가 깨진다. */
    @Override
    public TopicKey topicKey() {
        return TopicKey.NOTIFICATION_EVENTS;
    }
}
