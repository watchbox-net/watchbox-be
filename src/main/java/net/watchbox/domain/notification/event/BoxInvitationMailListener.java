package net.watchbox.domain.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberQueryService;
import net.watchbox.domain.notification.entity.NotificationChannel;
import net.watchbox.domain.notification.message.BoxInvitationMailFactory;
import net.watchbox.domain.notification.metrics.NotificationDeliveryMetrics;
import net.watchbox.domain.notification.service.MailSendService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 공유 박스 초대 → 초대받은 사람에게 안내 메일.
 *
 * <p><b>왜 초대만 메일을 보내나.</b> 웹앱이라 SSE 는 탭이 열려 있을 때만 닿는다. 콘텐츠 추가 같은 알림은
 * 다음에 접속할 때 알림함에서 보면 되지만, <b>초대는 상대의 응답(수락·거절)이 있어야 진행되는 알림</b>이라
 * 도달이 늦으면 기능 자체가 멈춘다. 그래서 초대에만 앱 밖 채널을 하나 더 둔다.
 * (콘텐츠 추가·초대 응답까지 메일을 보내면 스팸이 된다)
 *
 * <p><b>평범한 {@code @EventListener} 다.</b> 예전에는 {@code AFTER_COMMIT} + {@code @Async} 였는데,
 * 이제 {@code OutboxRelay} 가 <b>커밋 이후에</b> 트랜잭션 밖 스레드에서 발행하므로 두 장치가 모두 필요 없다.
 *
 * <p><b>예외를 삼키지 않는다.</b> 실패가 릴레이까지 올라가야 행이 미발행으로 남아 재시도된다.
 * 예전에는 삼키는 게 맞았다 — 알려봐야 아무도 재시도해줄 수 없었기 때문이다.
 *
 * <p>메일 주소는 이벤트 페이로드에 싣지 않는다. {@code BoxInvitationPayload} 는 SSE 로
 * 클라이언트까지 내려가는 값이라, 거기에 주소를 넣으면 남의 이메일이 노출된다. 여기서 조회한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoxInvitationMailListener {

    private final MemberQueryService memberQueryService;
    private final BoxInvitationMailFactory boxInvitationMailFactory;
    private final MailSendService mailSendService;
    private final NotificationDeliveryMetrics deliveryMetrics;

    @EventListener
    public void onBoxInvitationReceived(BoxInvitationReceivedEvent event) {
        Member receiver = memberQueryService.getByMemberIdOrThrow(event.receiverId());

        String email = receiver.getEmail();
        if (email == null || email.isBlank()) {
            // 소셜 로그인이 이메일을 주지 않은 계정이다. 보낼 곳이 없을 뿐 오류가 아니라
            // 재시도해도 소용없다 — 예외를 던지지 않고 정상 종료한다.
            deliveryMetrics.skipped(NotificationChannel.MAIL);
            log.info("[Mail] 발송 가능한 주소가 없어 초대 메일을 건너뜀 — memberId={}", event.receiverId());
            return;
        }

        mailSendService.send(email, boxInvitationMailFactory.boxInvitation(
                receiver.getNickname(), event.payload()));
    }
}
