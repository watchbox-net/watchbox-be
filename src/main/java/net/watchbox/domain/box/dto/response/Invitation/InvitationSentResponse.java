package net.watchbox.domain.box.dto.response.Invitation;

import lombok.Data;
import net.watchbox.domain.box.entity.request.InviteBoxRequest;
import net.watchbox.domain.box.entity.request.RequestStatus;

@Data
public class InvitationSentResponse { // 보낸초대 정보
    private Long requestId;
    private Long receiverId;
    private String receiver; // 초대 받는사람 닉네임
    private String sharedBoxTitle;
    private RequestStatus status;

    public static InvitationSentResponse from(InviteBoxRequest inviteBoxRequest) {
        InvitationSentResponse response = new InvitationSentResponse();
        response.setRequestId(inviteBoxRequest.getRequestId());
        response.setReceiverId(inviteBoxRequest.getReceiver().getMemberId());
        response.setReceiver(inviteBoxRequest.getReceiver().getNickname());
        response.setSharedBoxTitle(inviteBoxRequest.getBox().getTitle());
        response.setStatus(inviteBoxRequest.getStatus());
        return response;
    }
}
