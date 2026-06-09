package net.watchbox.domain.notification.dto.payload;

import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.invitation.BoxInvitation;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.entity.NotificationType;

/**
 * 공유 박스 초대 알림 페이로드.
 *
 * <p>예시 메시지: "{sender}님이 {boxName}에 초대했어요"
 */
public record BoxInvitationPayload(
        Long requestId,         // 초대 요청 ID (수락/거절 액션 시 필요)
        Long boxId,              // 초대된 박스 ID (수락 시 박스 페이지로 이동)
        String boxName,          // 박스 이름
        Long senderId,          // 초대한 사람 ID
        String sender,          // 초대한 사람 닉네임 스냅샷
        String senderProfileImage
) implements NotificationPayload {

    @Override
    public NotificationType type() {
        return NotificationType.BOX_INVITATION_RECEIVED;
    }

    public static BoxInvitationPayload of(BoxInvitation invitation, Box box, Member sender) {
        return new BoxInvitationPayload(
                invitation.getRequestId(),
                box.getBoxId(),
                box.getName(),
                sender.getMemberId(),
                sender.getNickname(),
                sender.getProfileImage()
        );
    }

}
