package net.watchbox.global.event;

/**
 * 도메인 이벤트 전송 경로.
 *
 * <p>Kafka 브로커는 상시 가동 비용이 있어 평소에는 꺼둔다. 그래서 코드를 바꾸지 않고
 * 런타임에 전환할 수 있어야 한다.
 */
public enum EventTransport {

    /** 애플리케이션 내부 이벤트(ApplicationEventPublisher). 브로커 불필요. */
    LOCAL,

    /** Kafka 토픽. 브로커가 떠 있어야 한다. */
    KAFKA
}
