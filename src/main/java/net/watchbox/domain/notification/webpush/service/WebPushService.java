package net.watchbox.domain.notification.webpush.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.webpush.entity.PushSubscription;
import net.watchbox.domain.notification.webpush.repository.PushSubscriptionRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Web Push 발송 서비스 (POC).
 *
 * <ul>
 *   <li>구독 등록/해제</li>
 *   <li>대상 멤버의 모든 구독에 푸시 전송</li>
 *   <li>410 Gone 응답 시 구독 자동 삭제 (브라우저 구독 만료 시)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushService {

    private final PushService pushService;
    private final PushSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    /** 브라우저 PushManager.subscribe() 결과를 DB 에 저장 (upsert). */
    @Transactional
    public void saveSubscription(Member member, String endpoint, String p256dh, String authSecret) {
        subscriptionRepository.findByEndpoint(endpoint).ifPresentOrElse(
                existing -> log.debug("Push subscription already registered. endpoint={}", endpoint),
                () -> subscriptionRepository.save(PushSubscription.builder()
                        .member(member)
                        .endpoint(endpoint)
                        .p256dh(p256dh)
                        .authSecret(authSecret)
                        .build())
        );
    }

    /**
     * 멤버의 모든 활성 구독에 Web Push 발송.
     * payload 는 JSON {"title":..., "body":...} 로 인코딩.
     */
    @Transactional
    public int sendToMember(Member member, String title, String body) {
        List<PushSubscription> subs = subscriptionRepository.findAllByMember(member);
        if (subs.isEmpty()) {
            log.warn("No push subscriptions found. memberId={}", member.getMemberId());
            return 0;
        }

        String payload = buildPayload(title, body);
        int delivered = 0;
        for (PushSubscription sub : subs) {
            if (sendOne(sub, payload)) {
                delivered++;
            }
        }
        return delivered;
    }

    /**
     * @return 전송 성공 여부. 410 Gone (구독 만료) 시 구독 삭제 후 false.
     */
    private boolean sendOne(PushSubscription sub, String payload) {
        try {
            Subscription subscription = new Subscription(
                    sub.getEndpoint(),
                    new Subscription.Keys(sub.getP256dh(), sub.getAuthSecret())
            );
            Notification notification = new Notification(subscription, payload);
            var response = pushService.send(notification);
            int status = response.getStatusLine().getStatusCode();
            if (status == 201 || status == 200) {
                log.debug("Web Push delivered. endpoint={}", sub.getEndpoint());
                return true;
            }
            if (status == 404 || status == 410) {
                // 구독 만료 — DB 에서 정리
                log.info("Web Push subscription expired ({}). Removing. endpoint={}", status, sub.getEndpoint());
                subscriptionRepository.deleteByEndpoint(sub.getEndpoint());
                return false;
            }
            log.warn("Web Push unexpected status={} endpoint={}", status, sub.getEndpoint());
            return false;
        } catch (GeneralSecurityException | JoseException | ExecutionException | InterruptedException | java.io.IOException e) {
            log.error("Web Push send failed. endpoint={}", sub.getEndpoint(), e);
            return false;
        }
    }

    private String buildPayload(String title, String body) {
        try {
            return objectMapper.writeValueAsString(Map.of("title", title, "body", body));
        } catch (JsonProcessingException e) {
            // 단순 문자열이라 실패 거의 없음
            return "{\"title\":\"" + title + "\",\"body\":\"" + body + "\"}";
        }
    }
}
