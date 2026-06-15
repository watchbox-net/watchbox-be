package net.watchbox.domain.notification.webpush.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Web Push VAPID 설정.
 *
 * <p>{@code public-key} / {@code private-key} 는 한 번 발급된 키쌍을 사용 (변경되면 모든 기존 구독 무효화).
 * 발급: {@code WebPushVapidKeyGenerator.main} 또는 https://vapidkeys.com
 */
@ConfigurationProperties(prefix = "web-push")
public record WebPushProperties(
        String publicKey,
        String privateKey,
        String subject
) {}
