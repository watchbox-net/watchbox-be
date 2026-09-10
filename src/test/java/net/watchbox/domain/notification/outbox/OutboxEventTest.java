package net.watchbox.domain.notification.outbox;

import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.notification.dto.payload.BoxInvitationPayload;
import net.watchbox.domain.notification.dto.payload.ContentBoxAddedPayload;
import net.watchbox.domain.notification.entity.NotificationType;
import net.watchbox.domain.notification.event.BoxInvitationReceivedEvent;
import net.watchbox.domain.notification.event.ContentBoxAddedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventTest {

    private static OutboxEvent invitationRow() {
        return OutboxEvent.from(new BoxInvitationReceivedEvent(
                7L, new BoxInvitationPayload(1L, 10L, "주말 영화", 2L, "현", null)));
    }

    @Test
    @DisplayName("기록 직후에는 미발행이고 시도 횟수가 0 이다")
    void startsUnpublished() {
        OutboxEvent row = invitationRow();

        assertThat(row.getPublishedAt()).isNull();
        assertThat(row.getAttempt()).isZero();
        assertThat(row.getEventId()).isNotBlank();
        assertThat(row.getNotificationType()).isEqualTo(NotificationType.BOX_INVITATION_RECEIVED);
    }

    @Test
    @DisplayName("수신자가 여럿이어도 순서 그대로 복원된다")
    void keepsReceiverOrder() {
        ContentBoxAddedPayload payload = new ContentBoxAddedPayload(
                10L, "주말 영화", BoxType.SHARED, 100L, 1030571L, "MOVIE",
                "마지막 흔적", null, 2L, "현", null);

        OutboxEvent row = OutboxEvent.from(new ContentBoxAddedEvent(List.of(5L, 6L, 7L), payload));

        assertThat(row.receiverIdList()).containsExactly(5L, 6L, 7L);
    }

    @Test
    @DisplayName("실패하면 시도 횟수가 늘고 다음 시도 시각이 잡힌다 — 행은 미발행으로 남는다")
    void failureKeepsRowPending() {
        OutboxEvent row = invitationRow();
        LocalDateTime next = LocalDateTime.now().plusSeconds(5);

        row.markFailed(next, "SMTP down");

        assertThat(row.getAttempt()).isEqualTo(1);
        assertThat(row.getNextAttemptAt()).isEqualTo(next);
        assertThat(row.getLastError()).isEqualTo("SMTP down");
        assertThat(row.getPublishedAt()).isNull();   // ← 재시도 대상으로 남아야 한다
    }

    @Test
    @DisplayName("발행에 성공하면 백오프와 직전 오류가 지워진다")
    void publishClearsRetryState() {
        OutboxEvent row = invitationRow();
        row.markFailed(LocalDateTime.now().plusSeconds(5), "SMTP down");
        LocalDateTime now = LocalDateTime.now();

        row.markPublished(now);

        assertThat(row.getPublishedAt()).isEqualTo(now);
        assertThat(row.getNextAttemptAt()).isNull();
        assertThat(row.getLastError()).isNull();
    }

    @Test
    @DisplayName("긴 오류 메시지는 컬럼 길이에 맞춰 잘린다")
    void truncatesLongError() {
        OutboxEvent row = invitationRow();

        row.markFailed(LocalDateTime.now(), "x".repeat(900));

        assertThat(row.getLastError()).hasSize(500);
    }
}
