package net.watchbox.domain.notification.dto.payload;

import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.invitation.BoxInvitation;
import net.watchbox.domain.box.entity.invitation.RequestStatus;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.entity.NotificationType;

/**
 * 공유 박스 초대 결과 알림 페이로드.
 * 수신자 = 초대를 보냈던 sender. 응답자(responder) = 초대를 받았던 receiver.
 *
 * <p>예시 메시지
 * <ul>
 *   <li>ACCEPTED: "{responder}님이 {boxName} 초대를 수락했어요"</li>
 *   <li>REJECTED: "{responder}님이 {boxName} 초대를 거절했어요"</li>
 * </ul>
 *
 * <p>{@code result} 가 PENDING 인 인스턴스는 만들지 않음 — 응답 직후의 결과만 알림으로 발송됨.
 */
public record BoxInvitationRespondedPayload(
        Long requestId,                  // 원본 초대 요청 ID
        Long boxId,                      // 초대 대상 박스 ID
        String boxName,                  // 박스 이름 스냅샷
        Long responderId,                // 초대 받은 사람 ID
        String responder,                // 초대 받은 사람 닉네임 스냅샷
        String responderProfileImage,    // 초대 받은 사람 프로필 이미지
        RequestStatus requestStatus     // ACCEPTED | REJECTED
) implements NotificationPayload {

    @Override
    public NotificationType type() {
        return NotificationType.BOX_INVITATION_RESPONDED;
    }

    public static BoxInvitationRespondedPayload of(BoxInvitation invitation, Box box, Member responder){
        return new BoxInvitationRespondedPayload(
                invitation.getRequestId(),
                box.getBoxId(),
                box.getName(),
                responder.getMemberId(),
                responder.getNickname(),
                responder.getProfileImage(),
                invitation.getStatus()
        );
    }
}
