package net.watchbox.domain.notification.dev;

import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.notification.entity.NotificationChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 채널별 실패를 인위로 일으켜 <b>도착률을 결정론적으로 측정</b>하기 위한 장치.
 *
 * <p><b>왜 kill -9 대신 이걸 쓰나.</b> 프로세스를 죽여서 유실을 재현하면 죽는 타이밍이 매번 달라
 * 유실 건수가 들쭉날쭉하다. 실패율 p 를 고정하면 같은 조건을 몇 번이든 반복할 수 있고,
 * <b>Outbox 도입 후 "p 와 무관하게 도착률 100%"</b> 라는 결론을 숫자로 보일 수 있다.
 * (프로세스 강제 종료 시나리오는 별도로 한 번만 정성 증거로 남긴다)
 *
 * <p><b>기본값은 0 이라 아무 일도 하지 않는다.</b> 값을 올릴 수 있는 경로는 DevInfra API 뿐이고,
 * 운영 프로파일에서는 그마저 거부한다. 빈 자체를 {@code @Profile} 로 빼지 않은 이유는
 * 호출부가 {@code ObjectProvider} 로 감싸져 읽기 나빠지기 때문이다 — 대신 값을 못 올리게 막는다.
 */
@Slf4j
@Component
public class NotificationFailureInjector {

    private static final String PRODUCTION_PROFILE = "prod";

    private final boolean adjustable;

    /** 발행 경로에서 매번 읽으므로 락 없이 volatile 로 둔다. */
    private final Map<NotificationChannel, Double> failureRates = new EnumMap<>(NotificationChannel.class);

    public NotificationFailureInjector(@Value("${spring.profiles.active:local}") String activeProfile) {
        this.adjustable = !PRODUCTION_PROFILE.equals(activeProfile);
        for (NotificationChannel channel : NotificationChannel.values()) {
            failureRates.put(channel, 0.0);
        }
    }

    /** 해당 채널의 실패율만큼 확률적으로 예외를 던진다. 실패율 0 이면 아무 일도 없다. */
    public void maybeFail(NotificationChannel channel) {
        double rate = failureRates.getOrDefault(channel, 0.0);
        if (rate > 0 && ThreadLocalRandom.current().nextDouble() < rate) {
            throw new InjectedFailureException(channel, rate);
        }
    }

    /**
     * @param rate 0.0 ~ 1.0
     * @throws IllegalStateException 운영 프로파일에서 호출한 경우
     */
    public double setFailureRate(NotificationChannel channel, double rate) {
        if (!adjustable) {
            throw new IllegalStateException("failure injection is not allowed in production");
        }
        if (rate < 0.0 || rate > 1.0) {
            throw new IllegalArgumentException("failure rate must be between 0.0 and 1.0: " + rate);
        }
        failureRates.put(channel, rate);
        // 켜둔 걸 잊고 "왜 알림이 안 오지" 를 디버깅하는 사고가 가장 흔하다. 눈에 띄게 남긴다.
        log.warn("[FailureInjector] 실패 주입 설정 — channel={}, rate={}", channel, rate);
        return rate;
    }

    public Map<NotificationChannel, Double> currentRates() {
        return Map.copyOf(failureRates);
    }

    /** 주입된 실패임을 로그에서 바로 구분하기 위한 전용 예외. */
    public static class InjectedFailureException extends RuntimeException {
        public InjectedFailureException(NotificationChannel channel, double rate) {
            super("injected failure for measurement - channel=%s, rate=%s".formatted(channel, rate));
        }
    }
}
