package net.watchbox.domain.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.notification.dev.NotificationFailureInjector;
import net.watchbox.domain.notification.entity.NotificationChannel;
import net.watchbox.domain.notification.message.MailMessage;
import net.watchbox.domain.notification.metrics.NotificationDeliveryMetrics;
import net.watchbox.global.properties.MailProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * SMTP 발송. 발송처(SES/Gmail 등)는 {@code spring.mail.*} 설정으로만 갈리고 이 코드는 바뀌지 않는다.
 *
 * <p><b>실패하면 {@link MailSendException} 을 던진다.</b> 호출자는 outbox 릴레이이고,
 * 예외를 받아야 행을 미발행으로 남겨 재시도한다. 여기서 삼키면 재시도가 사라진다.
 * (도메인 트랜잭션은 이미 커밋된 뒤라 초대 자체가 되돌아가지는 않는다)
 */
@Slf4j
@Service
public class MailSendService {

    /** 수신함에 표시될 발신자 이름. 서비스명이라 환경별로 갈리지 않는다. */
    private static final String FROM_NAME = "WatchBox";

    /** 이 프로파일이면 운영으로 보고 제목에 환경을 표시하지 않는다. */
    private static final String PRODUCTION_PROFILE = "prod";

    private final JavaMailSender mailSender;
    private final MailProperties properties;
    private final NotificationDeliveryMetrics deliveryMetrics;
    private final NotificationFailureInjector failureInjector;
    private final String subjectPrefix;

    public MailSendService(JavaMailSender mailSender, MailProperties properties,
                           NotificationDeliveryMetrics deliveryMetrics,
                           NotificationFailureInjector failureInjector,
                           @Value("${spring.profiles.active:local}") String activeProfile) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.deliveryMetrics = deliveryMetrics;
        this.failureInjector = failureInjector;
        this.subjectPrefix = subjectPrefixFor(activeProfile);
    }

    /**
     * 운영이 아니면 제목 앞에 환경을 표시한다 — 수신함에서 어느 서버가 보낸 메일인지 바로 갈린다.
     * 모르는 프로파일 이름은 표시하는 쪽으로 둔다. 접두가 잘못 붙는 것보다 운영인 줄 알고 넘어가는 쪽이 나쁘다.
     */
    static String subjectPrefixFor(String activeProfile) {
        if (activeProfile == null || activeProfile.isBlank() || PRODUCTION_PROFILE.equals(activeProfile)) {
            return "";
        }
        return "(" + activeProfile + " 환경) ";
    }

    /**
     * 한 통 발송.
     *
     * @param to 받는 주소. <b>호출 전에 발송 가능한 주소인지 확인해야 한다</b>
     *           (배달 불가 주소로 보내면 반송률이 올라 발송 자격을 잃는다)
     */
    public void send(String to, MailMessage message) {
        if (!properties.enabled()) {
            // 못 보낸 게 아니라 보내지 않기로 한 것이다. 실패로 세면 도착률이 왜곡된다.
            deliveryMetrics.skipped(NotificationChannel.MAIL);
            log.info("[Mail] 발송 비활성 상태 — 보내지 않음: to={}, subject={}",
                    to, subjectPrefix + message.subject());
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, message.hasHtml(), StandardCharsets.UTF_8.name());
            helper.setFrom(properties.from(), FROM_NAME);
            helper.setTo(to);
            helper.setSubject(subjectPrefix + message.subject());
            if (message.hasHtml()) {
                // 텍스트를 함께 실어야 HTML 을 못 읽는 클라이언트에서 빈 화면이 되지 않는다.
                helper.setText(message.text(), message.html());
            } else {
                helper.setText(message.text(), false);
            }
            // 측정용 실패 주입. 실제 SMTP 호출 직전이라 아래 catch 가 진짜 발송 실패와 똑같이 처리한다.
            failureInjector.maybeFail(NotificationChannel.MAIL);
            mailSender.send(mimeMessage);
            deliveryMetrics.success(NotificationChannel.MAIL);
            log.info("[Mail] 발송 완료 — subject={}", message.subject());
        } catch (UnsupportedEncodingException | jakarta.mail.MessagingException | RuntimeException e) {
            deliveryMetrics.failure(NotificationChannel.MAIL);
            log.warn("[Mail] 발송 실패 — subject={}, {}", message.subject(), e.getMessage());
            throw new MailSendException("mail send failed: " + message.subject(), e);
        }
    }
}
