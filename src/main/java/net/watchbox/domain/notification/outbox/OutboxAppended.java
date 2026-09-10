package net.watchbox.domain.notification.outbox;

/**
 * outbox 에 행이 추가됐다는 <b>내부 신호</b>. 도메인 이벤트가 아니라 릴레이를 깨우는 용도다.
 *
 * <p>이게 없으면 방금 기록한 알림이 다음 폴링 주기까지 기다린다. 커밋 직후 릴레이를 한 번
 * 깨워 그 지연을 없앤다. <b>이 신호가 유실돼도 폴링이 백스톱이라 보장은 깨지지 않는다</b> —
 * 그래서 전달을 보장할 필요가 없고 단순한 애플리케이션 이벤트로 충분하다.
 */
public record OutboxAppended() {

    public static final OutboxAppended INSTANCE = new OutboxAppended();
}
