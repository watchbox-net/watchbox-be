package net.watchbox.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.domain.notification.dto.payload.NotificationPayload;
import net.watchbox.domain.notification.entity.Notification;
import net.watchbox.domain.notification.entity.NotificationType;
import net.watchbox.domain.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public Notification createNotification(Long receiverId, NotificationType type, NotificationPayload payload) {
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
     * 스낵바 노출 ACK — 클라이언트에서 실제 토스트가 떴음을 알려옴.
     * 본인 소유 알림만 마킹 가능 (다른 사용자 알림은 영향 없음).
     */
    public void markSnackbarShown(Member member, List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) return;
        int updated = notificationRepository.markSnackbarShownByReceiverAndIds(member, notificationIds);
        log.debug("Marked snackbar shown. memberId={}, requested={}, updated={}",
                member.getMemberId(), notificationIds.size(), updated);
    }
}
