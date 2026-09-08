package net.watchbox.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 메일 발송 설정.
 * application.yml:
 * <pre>
 * notification:
 *   mail:
 *     enabled: false                        # 발송 스위치
 *     from: no-reply@watch-box.net          # 발신 주소
 *     support-address: admin@watch-box.net  # 본문 하단 문의처
 * </pre>
 *
 * @param enabled        false 면 발송하지 않고 로그만 남긴다. 로컬·테스트에서 실제 주소로 메일이 나가는 사고를 막는다
 * @param from           발신 주소. SES 에 인증된 도메인이어야 한다
 * @param supportAddress 본문 하단에 안내할 문의처
 */
@ConfigurationProperties(prefix = "notification.mail")
public record MailProperties(boolean enabled, String from, String supportAddress) {
}
