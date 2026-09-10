package net.watchbox.domain.notification.channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberQueryService;
import net.watchbox.domain.notification.entity.NotificationChannel;
import net.watchbox.domain.notification.event.BoxInvitationReceivedEvent;
import net.watchbox.domain.notification.event.NotificationEvent;
import net.watchbox.domain.notification.message.BoxInvitationMailFactory;
import net.watchbox.domain.notification.metrics.NotificationDeliveryMetrics;
import net.watchbox.domain.notification.service.MailSendService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * SES 메일. <b>초대에만</b> 보낸다.
 *
 * <p>웹앱이라 SSE 는 탭이 열려 있을 때만 닿는다. 콘텐츠 추가 같은 알림은 다음 접속 때 알림함에서
 * 보면 되지만, <b>초대는 상대의 응답이 있어야 진행되는 알림</b>이라 도달이 늦으면 기능이 멈춘다.
 * 그래서 초대에만 앱 밖 채널을 둔다 — 전부 보내면 스팸이다.
 *
 * <p><b>만회 경로가 없어 재시도 상한이 SSE 보다 높다.</b> 못 보내면 그걸로 끝이기 때문이다.
 *
 * <p>메일 주소는 이벤트 페이로드에 싣지 않는다. {@code BoxInvitationPayload} 는 SSE 로
 * 클라이언트까지 내려가는 값이라 거기에 주소를 넣으면 남의 이메일이 노출된다. 여기서 조회한다.
 */
@Slf4j
@Component
@Order(2)   // 외부 호출이라 뒤로. 순서가 바뀌어도 채널별 멱등 덕에 중복은 없다
@RequiredArgsConstructor
public class MailChannelHandler implements NotificationChannelHandler {

    /** 외부 서비스라 일시 실패가 잦다. 만회 경로가 없으니 SSE 보다 길게 붙든다. */
    private static final int MAX_ATTEMPT = 5;

    private final MemberQueryService memberQueryService;
    private final BoxInvitationMailFactory boxInvitationMailFactory;
    private final MailSendService mailSendService;
    private final NotificationDeliveryMetrics deliveryMetrics;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.MAIL;
    }

    @Override
    public boolean supports(NotificationEvent event) {
        return event instanceof BoxInvitationReceivedEvent;
    }

    @Override
    public int maxAttempt() {
        return MAX_ATTEMPT;
    }

    @Override
    @Transactional(readOnly = true)
    public void handle(NotificationEvent event) {
        BoxInvitationReceivedEvent invitation = (BoxInvitationReceivedEvent) event;
        Member receiver = memberQueryService.getByMemberIdOrThrow(invitation.receiverId());

        String email = receiver.getEmail();
        if (email == null || email.isBlank()) {
            // 소셜 로그인이 이메일을 주지 않은 계정이다. 몇 번을 시도해도 없는 건 없다.
            deliveryMetrics.skipped(NotificationChannel.MAIL);
            throw new NonRetryableDeliveryException(
                    "발송 가능한 주소 없음 - memberId=" + invitation.receiverId());
        }

        mailSendService.send(email, boxInvitationMailFactory.boxInvitation(
                receiver.getNickname(), invitation.payload()));
    }
}
