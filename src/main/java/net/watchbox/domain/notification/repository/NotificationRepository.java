package net.watchbox.domain.notification.repository;

import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** 수신자 기준 최신순 조회. size 는 Pageable.ofSize(n) 로 전달. */
    List<Notification> findByReceiverOrderByCreatedAtDesc(Member receiver, Pageable pageable);

    /** 안 읽음 카운트. */
    long countByReceiverAndIsReadFalse(Member receiver);

    /** 개별 조회 — 본인 소유 검증 포함. */
    Optional<Notification> findByNotificationIdAndReceiver(Long notificationId, Member receiver);

    /** 사용자의 모든 안 읽은 알림을 일괄 읽음 처리 (벌크 update). */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true " +
            "WHERE n.receiver = :receiver AND n.isRead = false")
    int markAllAsReadByReceiver(@Param("receiver") Member receiver);
}
