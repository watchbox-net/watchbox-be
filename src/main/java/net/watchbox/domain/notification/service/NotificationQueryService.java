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

    private final NotificationRepository notificationRepository;

    /** 인박스 — 최신순 size 개. */
    public List<Notification> getRecent(Member member, int size) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(
                member, Pageable.ofSize(size));
    }

    /** 안 읽음 카운트 (헤더 배지용). */
    public long countUnread(Member member) {
        return notificationRepository.countByReceiverAndIsReadFalse(member);
    }
}
