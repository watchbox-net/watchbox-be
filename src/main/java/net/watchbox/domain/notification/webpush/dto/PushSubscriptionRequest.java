package net.watchbox.domain.notification.webpush.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 브라우저 PushManager.subscribe() 결과를 서버로 보낼 때의 요청 바디.
 * 브라우저 PushSubscription.toJSON() 구조와 동일.
 */
public record PushSubscriptionRequest(
        @NotBlank String endpoint,
        @NotBlank Keys keys
) {
    public record Keys(
            @NotBlank String p256dh,
            @NotBlank String auth
    ) {}
}
