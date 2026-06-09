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

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * SSE 구독 시 catchup 대상 조회 — 아직 스낵바로 노출되지 않은(snackbarShown=false) 알림 중 최신순 N개.
     * freshness 만료 여부는 코드에서 NotificationResponse.from 으로 row 별 판정.
     */
    List<Notification> findByReceiverAndSnackbarShownFalseOrderByCreatedAtDesc(
            Member receiver, Pageable pageable);

    /**
     * 클라이언트 스낵바 노출 ACK — 본인 소유 알림들을 일괄 snackbarShown=true 처리.
     * receiver 조건으로 타인 알림 마킹 방지.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.snackbarShown = true " +
            "WHERE n.receiver = :receiver " +
            "AND n.notificationId IN :notificationIds " +
            "AND n.snackbarShown = false")
    int markSnackbarShownByReceiverAndIds(@Param("receiver") Member receiver,
                                          @Param("notificationIds") List<Long> notificationIds);
}
