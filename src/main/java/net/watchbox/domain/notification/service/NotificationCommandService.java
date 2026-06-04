package net.watchbox.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.domain.notification.dto.payload.NotificationPayload;
import net.watchbox.domain.notification.entity.Notification;
import net.watchbox.domain.notification.entity.NotificationType;
import net.watchbox.domain.notification.repository.NotificationRepository;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final MemberService memberService;

    /**
     * 알림 1건 생성 (receiver 한 명당 row 1개 — fan-out on write 시 호출 반복).
     */
    public Notification create(Long receiverId, NotificationType type, NotificationPayload payload) {
        Member receiver = memberService.getByMemberIdOrThrow(receiverId);
        return notificationRepository.save(
                Notification.builder()
                        .receiver(receiver)
                        .notificationType(type)
                        .payload(payload)
                        .build()
        );
    }

    /**
     * 개별 읽음 처리 — 본인 소유 알림이어야 함.
     */
    public void markAsRead(Member member, Long notificationId) {
        Notification notification = notificationRepository
                .findByNotificationIdAndReceiver(notificationId, member)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND, notificationId));
        notification.markAsRead();
    }

    /**
     * 사용자의 모든 안 읽은 알림 일괄 읽음 처리.
     */
    public void markAllAsRead(Member member) {
        int updated = notificationRepository.markAllAsReadByReceiver(member);
        log.debug("Marked all as read. memberId={}, count={}", member.getMemberId(), updated);
    }
}
