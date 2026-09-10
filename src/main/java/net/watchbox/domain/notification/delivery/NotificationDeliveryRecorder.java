package net.watchbox.domain.notification.delivery;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.notification.entity.NotificationChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 채널별 전달 결과를 남긴다.
 *
 * <p><b>모두 {@code REQUIRES_NEW} 여야 한다.</b> 발송 트랜잭션에 얹으면 그게 롤백될 때
 * 기록도 같이 사라져 <b>재시도가 또 보낸다</b> — 아무것도 막지 못한다.
 * 기록은 발송 결과와 독립적으로 살아남아야 한다.
 */
@Service
@RequiredArgsConstructor
public class NotificationDeliveryRecorder {

    private final NotificationDeliveryRepository notificationDeliveryRepository;

    /** 이미 끝난 채널인지. SENT 든 FAILED 든 다시 시도하지 않는다. */
    @Transactional(readOnly = true)
    public boolean isTerminal(String eventId, NotificationChannel channel) {
        return notificationDeliveryRepository.findByEventIdAndChannel(eventId, channel)
                .map(delivery -> delivery.getStatus().isTerminal())
                .orElse(false);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(String eventId, NotificationChannel channel) {
        NotificationDelivery delivery = findOrCreate(eventId, channel);
        delivery.markSent(LocalDateTime.now());
        notificationDeliveryRepository.save(delivery);
    }

    /**
     * 재시도 여지가 있는 실패.
     *
     * @return 갱신된 상태. {@link DeliveryStatus#FAILED} 면 상한에 닿아 종결된 것이다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DeliveryStatus markFailed(String eventId, NotificationChannel channel,
                                     Throwable cause, int maxAttempt) {
        NotificationDelivery delivery = findOrCreate(eventId, channel);
        delivery.markFailed(String.valueOf(cause), maxAttempt);
        notificationDeliveryRepository.save(delivery);
        return delivery.getStatus();
    }

    /** 재시도해도 소용없는 실패. 상한을 기다리지 않고 바로 종결한다. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void abandon(String eventId, NotificationChannel channel, Throwable cause) {
        NotificationDelivery delivery = findOrCreate(eventId, channel);
        delivery.abandon(String.valueOf(cause));
        notificationDeliveryRepository.save(delivery);
    }

    private NotificationDelivery findOrCreate(String eventId, NotificationChannel channel) {
        return notificationDeliveryRepository.findByEventIdAndChannel(eventId, channel)
                .orElseGet(() -> NotificationDelivery.of(eventId, channel));
    }
}
