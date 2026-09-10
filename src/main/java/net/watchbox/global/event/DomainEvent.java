package net.watchbox.global.event;

/**
 * 전송 경로(로컬 큐 / Kafka)와 무관하게 발행할 수 있는 도메인 이벤트.
 *
 * <p>아래 두 값은 <b>Kafka 경로에서만</b> 쓰인다. 로컬 경로는 Spring 이 클래스 타입으로
 * 리스너를 찾으므로 필요 없다. 그래도 이벤트가 직접 들고 있게 한 이유는, 전송 방식을 바꿔도
 * 발행부 코드가 그대로여야 하기 때문이다.
 */
public interface DomainEvent {

    /** 라우팅 이름. 로깅·디버깅과 향후 토픽 분기에 쓴다. */
    String type();

    /**
     * 파티션 키. <b>같은 키의 이벤트는 순서가 보장된다.</b>
     * 사용자별 순서가 중요한 알림은 memberId 를 쓴다.
     */
    String partitionKey();

    /**
     * 나갈 논리 토픽. 기본은 도메인 이벤트 토픽이고, 종류가 다른 메시지는 따로 나눈다.
     *
     * <p>같은 토픽에 타입이 섞이면 컨슈머가 남의 메시지를 자기 타입으로 읽으려다 깨진다
     * (전역 설정이 타입 헤더를 쓰지 않아 컨슈머가 대상 타입을 고정한다).
     */
    default TopicKey topicKey() {
        return TopicKey.DOMAIN_EVENTS;
    }
}
