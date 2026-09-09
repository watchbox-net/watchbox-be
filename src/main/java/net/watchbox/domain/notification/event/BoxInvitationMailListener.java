package net.watchbox.domain.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberQueryService;
import net.watchbox.domain.notification.entity.NotificationChannel;
import net.watchbox.domain.notification.message.BoxInvitationMailFactory;
import net.watchbox.domain.notification.metrics.NotificationDeliveryMetrics;
import net.watchbox.domain.notification.service.MailSendService;
import net.watchbox.global.config.AsyncConfig;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 공유 박스 초대 → 초대받은 사람에게 안내 메일.
 *
 * <p><b>왜 초대만 메일을 보내나.</b> 웹앱이라 SSE 는 탭이 열려 있을 때만 닿는다. 콘텐츠 추가 같은 알림은
 * 다음에 접속할 때 알림함에서 보면 되지만, <b>초대는 상대의 응답(수락·거절)이 있어야 진행되는 알림</b>이라
 * 도달이 늦으면 기능 자체가 멈춘다. 그래서 초대에만 앱 밖 채널을 하나 더 둔다.
 * (콘텐츠 추가·초대 응답까지 메일을 보내면 스팸이 된다)
 *
 * <p><b>{@code AFTER_COMMIT} 이어야 한다.</b> 커밋 전에 보내면 롤백된 초대의 메일이 나가는데, 메일은 취소가 안 된다.
 *
 * <p><b>{@code @Async} 여야 한다.</b> SMTP 왕복이 수백 ms~수 초라, 동기로 하면 그만큼 초대 API 가 늦어진다.
 * SSE 와 풀을 나눈 이유는 격리다 — {@link AsyncConfig} 참고.
 *
 * <p><b>트랜잭션은 {@code REQUIRES_NEW} 여야 한다.</b> AFTER_COMMIT 은 원래 트랜잭션이 이미 커밋된 뒤라
 * 거기에 참여할 수 없고, Spring 이 그 조합을 기동 시점에 막는다. 새 트랜잭션을 열어 주소를 조회한다.
 *
 * <p><b>메일 주소는 이벤트 페이로드에 싣지 않는다.</b> {@code BoxInvitationPayload} 는 SSE 로
 * 클라이언트까지 내려가는 값이라, 거기에 주소를 넣으면 남의 이메일이 노출된다. 여기서 조회한다.
 *
 * <p><b>예외를 삼킨다.</b> 메일 실패가 이미 커밋된 초대에 영향을 주면 안 된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoxInvitationMailListener {

    private final MemberQueryService memberQueryService;
    private final BoxInvitationMailFactory boxInvitationMailFactory;
    private final MailSendService mailSendService;
    private final NotificationDeliveryMetrics deliveryMetrics;

    @Async(AsyncConfig.MAIL_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onBoxInvitationReceived(BoxInvitationReceivedEvent event) {
        try {
            Member receiver = memberQueryService.getByMemberIdOrThrow(event.receiverId());
            String email = receiver.getEmail();
            if (email == null || email.isBlank()) {
                // 소셜 로그인이 이메일을 주지 않은 계정이다. 보낼 곳이 없을 뿐 오류는 아니다.
                deliveryMetrics.skipped(NotificationChannel.MAIL);
                log.info("[Mail] 발송 가능한 주소가 없어 초대 메일을 건너뜀 — memberId={}", event.receiverId());
                return;
            }
            mailSendService.send(email, boxInvitationMailFactory.boxInvitation(
                    receiver.getNickname(), event.payload()));
        } catch (Exception e) {
            // 수신자 조회 단계의 실패다. 발송 자체의 실패는 MailSendService 안에서 센다.
            deliveryMetrics.failure(NotificationChannel.MAIL);
            log.warn("[Mail] 박스 초대 안내 발송 실패 — receiverId={}, {}", event.receiverId(), e.getMessage());
        }
    }
}
