package net.watchbox.domain.notification.dev;

import net.watchbox.domain.notification.entity.NotificationChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 측정용 장치지만 <b>운영에서 켜지면 알림이 실제로 사라진다.</b> 그 방어를 테스트로 고정한다.
 */
class NotificationFailureInjectorTest {

    @Test
    @DisplayName("기본값은 0 이라 아무 일도 하지 않는다")
    void doesNothingByDefault() {
        NotificationFailureInjector injector = new NotificationFailureInjector("local");

        assertThatCode(() -> {
            for (int i = 0; i < 1_000; i++) {
                injector.maybeFail(NotificationChannel.MAIL);
                injector.maybeFail(NotificationChannel.SSE);
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패율 1.0 이면 항상 던진다")
    void alwaysFailsAtRateOne() {
        NotificationFailureInjector injector = new NotificationFailureInjector("local");
        injector.setFailureRate(NotificationChannel.MAIL, 1.0);

        assertThatThrownBy(() -> injector.maybeFail(NotificationChannel.MAIL))
                .isInstanceOf(NotificationFailureInjector.InjectedFailureException.class);
    }

    @Test
    @DisplayName("채널마다 독립이다 — 메일만 켜도 SSE 는 멀쩡하다")
    void ratesAreIndependentPerChannel() {
        NotificationFailureInjector injector = new NotificationFailureInjector("local");
        injector.setFailureRate(NotificationChannel.MAIL, 1.0);

        assertThatCode(() -> injector.maybeFail(NotificationChannel.SSE)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("운영 프로파일에서는 켤 수 없다")
    void cannotArmInProduction() {
        NotificationFailureInjector injector = new NotificationFailureInjector("prod");

        assertThatThrownBy(() -> injector.setFailureRate(NotificationChannel.MAIL, 1.0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(injector.currentRates()).containsEntry(NotificationChannel.MAIL, 0.0);
    }

    @Test
    @DisplayName("0.0~1.0 범위를 벗어나면 거부한다")
    void rejectsOutOfRange() {
        NotificationFailureInjector injector = new NotificationFailureInjector("local");

        assertThatThrownBy(() -> injector.setFailureRate(NotificationChannel.SSE, 1.5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> injector.setFailureRate(NotificationChannel.SSE, -0.1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
