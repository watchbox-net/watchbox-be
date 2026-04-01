package net.watchbox.domain.box.dto.Invitation;

import lombok.Data;
import net.watchbox.domain.box.entity.invitation.BoxInvitation;
import net.watchbox.domain.box.entity.invitation.RequestStatus;

@Data
public class InvitationReceivedResponse { // 받은초대 정보
    private Long requestId;
    private Long senderId;
    private String sender; // 초대 보낸사람 닉네임
    private String sharedBoxTitle;
    private RequestStatus status;

    public static InvitationReceivedResponse from(BoxInvitation boxInvitation) {
        InvitationReceivedResponse response = new InvitationReceivedResponse();
        response.setRequestId(boxInvitation.getRequestId());
        response.setSenderId(boxInvitation.getSender().getMemberId());
        response.setSender(boxInvitation.getSender().getNickname());
        response.setSharedBoxTitle(boxInvitation.getBox().getName());
        response.setStatus(boxInvitation.getStatus());
        return response;
    }
}
