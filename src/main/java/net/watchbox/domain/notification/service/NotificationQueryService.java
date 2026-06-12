package net.watchbox.domain.notification.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.entity.Notification;
import net.watchbox.domain.notification.repository.NotificationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    /** SSE 구독 시 catchup 으로 푸시할 알림 최대 개수. */
    private static final int CATCHUP_SIZE = 5;

    private final NotificationRepository notificationRepository;

    /**
     * SSE 구독 시 catchup 후보 조회 — snackbarShown=false 인 알림 최신순 최대 N개.
     * freshness 만료 검사는 호출 측에서 NotificationResponse.from 으로 row 별 처리.
     */
    public List<Notification> getCatchupCandidates(Member member) {
        return notificationRepository.findByReceiverAndSnackbarShownFalseOrderByCreatedAtDesc(
                member, Pageable.ofSize(CATCHUP_SIZE));
    }
}
