package net.watchbox.domain.notification.webpush.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 브라우저 PushManager.subscribe() 결과를 서버로 보낼 때의 요청 바디.
 * 브라우저 PushSubscription.toJSON() 구조와 동일.
 */
public record PushSubscriptionRequest(
        @NotBlank String endpoint,
        // 중첩 객체엔 @NotBlank(문자열 전용) 불가 → @NotNull + @Valid(내부 필드 검증 cascade)
        @NotNull @Valid Keys keys
) {
    public record Keys(
            @NotBlank String p256dh,
            @NotBlank String auth
    ) {}
}
