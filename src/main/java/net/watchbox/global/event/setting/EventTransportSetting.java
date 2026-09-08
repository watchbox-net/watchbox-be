package net.watchbox.global.event.setting;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.watchbox.global.entity.BaseTime;
import net.watchbox.global.event.EventTransport;

/**
 * 도메인 이벤트 전송 경로 설정. <b>행 하나만 존재</b>한다(id 고정).
 *
 * <p>환경변수가 아니라 DB 에 두는 이유: Kafka 브로커를 필요할 때만 켜기 때문에
 * <b>재배포 없이 런타임에 전환</b>할 수 있어야 한다.
 */
@Entity
@Table(name = "event_transport_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventTransportSetting extends BaseTime {

    /** 단일 행 보장을 위한 고정 id. */
    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventTransport transport;

    private EventTransportSetting(EventTransport transport) {
        this.id = SINGLETON_ID;
        this.transport = transport;
    }

    public static EventTransportSetting of(EventTransport transport) {
        return new EventTransportSetting(transport);
    }

    public void changeTo(EventTransport transport) {
        this.transport = transport;
    }
}
