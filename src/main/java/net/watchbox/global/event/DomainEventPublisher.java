package net.watchbox.global.event;

/**
 * 도메인 이벤트 발행 진입점. 발행부는 이 인터페이스만 알고, 실제 전송 경로는 모른다.
 *
 * <p>주입받을 때는 이 타입으로 받으면 된다 — {@code SwitchingEventPublisher} 가
 * {@code @Primary} 로 등록되어 설정값에 따라 위임한다.
 */
public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
