package net.watchbox.domain.box.dto.response.Invitation;

import lombok.Data;
import net.watchbox.domain.box.entity.request.BoxInvitation;
import net.watchbox.domain.box.entity.request.RequestStatus;

@Data
public class InvitationSentResponse { // 보낸초대 정보
    private Long requestId;
    private Long receiverId;
    private String receiver; // 초대 받는사람 닉네임
    private String sharedBoxTitle;
    private RequestStatus status;

    public static InvitationSentResponse from(BoxInvitation boxInvitation) {
        InvitationSentResponse response = new InvitationSentResponse();
        response.setRequestId(boxInvitation.getRequestId());
        response.setReceiverId(boxInvitation.getReceiver().getMemberId());
        response.setReceiver(boxInvitation.getReceiver().getNickname());
        response.setSharedBoxTitle(boxInvitation.getBox().getName());
        response.setStatus(boxInvitation.getStatus());
        return response;
    }
}
