package net.watchbox.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.dto.response.NotificationResponse;
import net.watchbox.domain.notification.facade.NotificationFacade;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
@Slf4j
@Tag(name = "Notification", description = "알림 API (인박스 + 실시간 SSE)")
public class NotificationController {

    private final NotificationFacade notificationFacade;

    @Operation(summary = "SSE 구독",
            description = "Server-Sent Events 로 실시간 알림 수신. " +
                    "연결 직후 'connect' 이벤트 1회, 이후 'notification' 이벤트로 알림 푸시. " +
                    "timeout 시 클라이언트가 재연결 필요.")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal Member member) {
        return notificationFacade.subscribe(member);
    }

    @Operation(summary = "인박스 조회", description = "최근 알림 목록 (현재 최대 50개)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getInbox(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationFacade.getInbox(member)));
    }

    @Operation(summary = "안 읽은 알림 개수", description = "헤더 배지 표시용")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationFacade.getUnreadCount(member)));
    }

    @Operation(summary = "알림 개별 읽음 처리")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal Member member,
            @PathVariable Long notificationId
    ) {
        notificationFacade.markAsRead(member, notificationId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "알림 전체 읽음 처리", description = "사용자의 모든 안 읽은 알림을 일괄 읽음 처리")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal Member member
    ) {
        notificationFacade.markAllAsRead(member);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
