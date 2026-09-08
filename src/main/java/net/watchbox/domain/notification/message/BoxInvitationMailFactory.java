package net.watchbox.domain.notification.message;

import net.watchbox.domain.notification.dto.payload.BoxInvitationPayload;
import net.watchbox.global.properties.MailProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 공유 박스 초대 메일의 제목·본문 생성.
 *
 * <p><b>지금은 텍스트 본문만 만든다.</b> 메일 HTML 은 브라우저와 달라서(Outlook 은 Word 엔진이라
 * flex·grid 를 모르고, Gmail 은 {@code <style>} 을 지운다) 표 레이아웃 + 인라인 스타일로 따로 짜야 한다.
 * 디자인이 나오면 {@link MailMessage#html()} 을 채우고, 텍스트 본문은 대체본으로 그대로 남긴다.
 *
 * <p>문구를 여기 모아두는 이유는 <b>테스트로 고정할 값</b>이라서다. 프로퍼티를 생성자로 받으면
 * 스프링 컨텍스트 없이도 이 클래스만 만들어 본문을 검증할 수 있다.
 */
@Component
public class BoxInvitationMailFactory {

    private final MailProperties properties;
    private final String serviceUrl;

    public BoxInvitationMailFactory(MailProperties properties, @Value("${url.service}") String serviceUrl) {
        this.properties = properties;
        this.serviceUrl = serviceUrl;
    }

    public MailMessage boxInvitation(String receiverNickname, BoxInvitationPayload payload) {
        String subject = "%s님이 '%s' 박스에 초대했어요".formatted(payload.sender(), payload.boxName());
        return MailMessage.textOnly(subject, text(receiverNickname, payload));
    }

    private String text(String receiverNickname, BoxInvitationPayload payload) {
        return """
                %s님, 공유 박스 초대가 도착했어요.

                %s님이 회원님을 '%s' 박스에 초대했습니다.
                WatchBox 에서 초대를 수락하면 함께 박스를 채울 수 있어요.

                  초대한 사람   %s
                  박스 이름     %s

                초대 확인하기: %s

                ---
                본 메일은 공유 박스 초대 안내 메일로, 발신 전용입니다.
                문의사항은 %s 으로 연락해 주세요.
                """.formatted(
                receiverNickname,
                payload.sender(), payload.boxName(),
                payload.sender(), payload.boxName(),
                serviceUrl,
                properties.supportAddress());
    }
}
