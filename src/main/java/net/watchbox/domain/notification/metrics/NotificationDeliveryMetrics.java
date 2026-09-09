package net.watchbox.domain.notification.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.notification.entity.NotificationChannel;
import org.springframework.stereotype.Component;

/**
 * 채널별 알림 전달 결과 카운터.
 *
 * <p><b>도착률을 재려고 만들었다.</b> 지금 구조는 실패가 {@code log.warn} 으로만 남아서
 * "몇 건이 안 갔는지" 를 셀 수가 없다. 발송한 초대 수(N) 대비 {@code success} 를 비교하면
 * 도착률이 나오고, 그게 Outbox 도입 전후를 비교할 유일한 정량 지표다.
 *
 * <p>측정용으로 만들었지만 <b>이후에도 그대로 남는다</b> — 운영에서 채널별 실패율을 보는 지표가 된다.
 * 카운터는 리셋할 수 없으므로, 측정할 때는 시작·종료 시점 스냅샷을 찍어 차를 본다.
 *
 * <p>메트릭 이름: {@code notification.delivery} / 태그: {@code channel}, {@code result}
 */
@Component
@RequiredArgsConstructor
public class NotificationDeliveryMetrics {

    public static final String METER_NAME = "notification.delivery";

    private final MeterRegistry meterRegistry;

    public void success(NotificationChannel channel) {
        count(channel, "success");
    }

    public void failure(NotificationChannel channel) {
        count(channel, "failure");
    }

    /**
     * 보낼 수 있는 상태가 아니어서 시도조차 하지 않은 경우(발송 비활성, 주소 없음 등).
     * 실패로 세면 도착률이 왜곡된다 — 분모에서 빼야 할 건이지 못 보낸 건이 아니다.
     */
    public void skipped(NotificationChannel channel) {
        count(channel, "skipped");
    }

    private void count(NotificationChannel channel, String result) {
        Counter.builder(METER_NAME)
                .tag("channel", channel.name())
                .tag("result", result)
                .description("채널별 알림 전달 결과")
                .register(meterRegistry)
                .increment();
    }
}
