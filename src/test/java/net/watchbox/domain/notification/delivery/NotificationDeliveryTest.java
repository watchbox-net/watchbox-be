package net.watchbox.domain.notification.delivery;

import net.watchbox.domain.notification.entity.NotificationChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationDeliveryTest {

    private static final int MAX_ATTEMPT = 3;

    private static NotificationDelivery mail() {
        return NotificationDelivery.of("evt-1", NotificationChannel.MAIL);
    }

    @Test
    @DisplayName("처음에는 PENDING 이고 아직 끝나지 않은 상태다")
    void startsPending() {
        NotificationDelivery delivery = mail();

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(delivery.getStatus().isTerminal()).isFalse();
        assertThat(delivery.getAttempt()).isZero();
    }

    @Test
    @DisplayName("상한 전까지는 PENDING 으로 남아 다시 시도된다")
    void staysPendingBeforeMaxAttempt() {
        NotificationDelivery delivery = mail();

        delivery.markFailed("SMTP down", MAX_ATTEMPT);

        assertThat(delivery.getAttempt()).isEqualTo(1);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(delivery.getLastError()).isEqualTo("SMTP down");
    }

    @Test
    @DisplayName("상한에 닿으면 FAILED 로 종결돼 무한 재시도를 막는다")
    void becomesFailedAtMaxAttempt() {
        NotificationDelivery delivery = mail();

        for (int i = 0; i < MAX_ATTEMPT; i++) {
            delivery.markFailed("SMTP down", MAX_ATTEMPT);
        }

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(delivery.getStatus().isTerminal()).isTrue();
    }

    @Test
    @DisplayName("재시도해도 소용없는 실패는 첫 시도에 바로 종결된다")
    void abandonIsImmediatelyTerminal() {
        NotificationDelivery delivery = mail();

        delivery.abandon("발송 가능한 주소 없음");

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(delivery.getAttempt()).isEqualTo(1);   // 상한(3)에 한참 못 미쳐도 종결
    }

    @Test
    @DisplayName("성공하면 SENT 로 닫히고 직전 오류가 지워진다")
    void sentClearsError() {
        NotificationDelivery delivery = mail();
        delivery.markFailed("SMTP down", MAX_ATTEMPT);
        LocalDateTime now = LocalDateTime.now();

        delivery.markSent(now);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(delivery.getStatus().isTerminal()).isTrue();
        assertThat(delivery.getSentAt()).isEqualTo(now);
        assertThat(delivery.getLastError()).isNull();
    }

    @Test
    @DisplayName("긴 오류 메시지는 컬럼 길이에 맞춰 잘린다")
    void truncatesLongError() {
        NotificationDelivery delivery = mail();

        delivery.markFailed("x".repeat(900), MAX_ATTEMPT);

        assertThat(delivery.getLastError()).hasSize(500);
    }
}
