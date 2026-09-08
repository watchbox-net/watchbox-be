package net.watchbox.domain.notification.message;

import net.watchbox.domain.notification.dto.payload.BoxInvitationPayload;
import net.watchbox.global.properties.MailProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 본문 문구는 사용자에게 그대로 보이는 값이라 고정해둔다.
 * 스프링 컨텍스트 없이 생성자로만 만들 수 있어야 이 테스트가 가능하다.
 */
class BoxInvitationMailFactoryTest {

    private static final String SERVICE_URL = "https://watch-box.net";
    private static final String SUPPORT = "admin@watch-box.net";

    private final BoxInvitationMailFactory factory = new BoxInvitationMailFactory(
            new MailProperties(true, "no-reply@watch-box.net", SUPPORT), SERVICE_URL);

    private static BoxInvitationPayload payload() {
        return new BoxInvitationPayload(1L, 10L, "주말 영화", 2L, "현", null);
    }

    @Test
    @DisplayName("제목에 초대한 사람과 박스 이름이 들어간다")
    void subject() {
        MailMessage message = factory.boxInvitation("받는이", payload());

        assertThat(message.subject()).isEqualTo("현님이 '주말 영화' 박스에 초대했어요");
    }

    @Test
    @DisplayName("본문에 수신자·초대자·박스명·바로가기·문의처가 들어간다")
    void text() {
        MailMessage message = factory.boxInvitation("받는이", payload());

        assertThat(message.text())
                .contains("받는이님")
                .contains("현님")
                .contains("주말 영화")
                .contains(SERVICE_URL)
                .contains(SUPPORT);
    }

    @Test
    @DisplayName("디자인 전이라 HTML 없이 텍스트로만 나간다")
    void textOnly() {
        MailMessage message = factory.boxInvitation("받는이", payload());

        assertThat(message.hasHtml()).isFalse();
    }
}
