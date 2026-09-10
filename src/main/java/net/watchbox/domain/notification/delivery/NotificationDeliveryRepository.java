package net.watchbox.domain.notification.delivery;

import net.watchbox.domain.notification.entity.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {

    Optional<NotificationDelivery> findByEventIdAndChannel(String eventId, NotificationChannel channel);

    List<NotificationDelivery> findByEventId(String eventId);

    /** 사람이 봐야 하는 종결 실패. 운영 조회용. */
    long countByStatus(DeliveryStatus status);
}
