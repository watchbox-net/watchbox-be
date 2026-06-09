package net.watchbox.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.facade.NotificationFacade;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
@Slf4j
@Tag(name = "Notification", description = "알림 API (실시간 SSE 스낵바 전용)")
public class NotificationController {

    private final NotificationFacade notificationFacade;

    @Operation(summary = "SSE 구독 + catchup",
            description = "Server-Sent Events 로 실시간 알림 수신. " +
                    "연결 직후 'connect' 핸드셰이크 1회, " +
                    "이어서 미노출(snackbarShown=false) 알림 최대 5개를 'notification' 이벤트로 catchup 푸시. " +
                    "이후 새로 발생하는 알림도 같은 채널로 푸시됨. " +
                    "timeout 시 클라이언트가 재연결 필요.")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal Member member) {
        return notificationFacade.subscribe(member);
    }

    @Operation(summary = "스낵바 노출 ACK",
            description = "클라이언트가 알림을 스낵바로 실제 노출했음을 서버에 알림. " +
                    "이후 같은 알림이 스낵바로 다시 뜨지 않도록 snackbarShown=true 마킹. " +
                    "body 로 노출 완료한 notificationId 목록을 보냄 (배치).")
    @PostMapping("/snackbar-shown")
    public ResponseEntity<ApiResponse<Void>> markSnackbarShown(
            @AuthenticationPrincipal Member member,
            @RequestBody List<Long> notificationIds
    ) {
        notificationFacade.markSnackbarShown(member, notificationIds);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
