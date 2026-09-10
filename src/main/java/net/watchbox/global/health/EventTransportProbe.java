package net.watchbox.global.health;

import net.watchbox.global.event.DomainEvent;

/**
 * 전송 경로 왕복 확인용 프로브 이벤트. 실제 도메인 의미는 없다.
 *
 * @param probeId 요청마다 유일한 값. 수신 측이 어느 요청의 응답인지 매칭하는 데 쓴다.
 * @param value   왕복 후 값이 보존됐는지 확인할 페이로드
 */
public record EventTransportProbe(String probeId, String value) implements DomainEvent {

    public static final String TYPE = "transport-probe";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public String partitionKey() {
        return probeId;
    }
}
