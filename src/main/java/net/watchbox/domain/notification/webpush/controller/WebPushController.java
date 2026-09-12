package net.watchbox.domain.notification.webpush.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.properties.WebPushProperties;
import net.watchbox.domain.notification.webpush.dto.PushSubscriptionRequest;
import net.watchbox.domain.notification.webpush.service.WebPushService;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/web-push")
@RequiredArgsConstructor
@Tag(name = "1-3 [Member] Web Push")
public class WebPushController {

    private final WebPushService webPushService;
    private final WebPushProperties webPushProperties;

    @Operation(summary = "VAPID 공개키 조회",
            description = "프론트의 PushManager.subscribe({ applicationServerKey }) 에 사용. " +
                    "환경변수 WEB_PUSH_PUBLIC_KEY 값 그대로 반환.")
    @GetMapping("/public-key")
    public ResponseEntity<ApiResponse<Map<String, String>>> getPublicKey() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("publicKey", webPushProperties.publicKey())));
    }

    @Operation(summary = "Web Push 구독 등록",
            description = "브라우저가 발급받은 PushSubscription 정보를 서버에 저장. " +
                    "이후 이 멤버 앞으로 Push 발송 시 사용됨.")
    @PostMapping("/subscriptions")
    public ResponseEntity<ApiResponse<Void>> subscribe(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid PushSubscriptionRequest request
    ) {
        webPushService.saveSubscription(
                member,
                request.endpoint(),
                request.keys().p256dh(),
                request.keys().auth()
        );
        return ResponseEntity.ok(ApiResponse.success());
    }
}
