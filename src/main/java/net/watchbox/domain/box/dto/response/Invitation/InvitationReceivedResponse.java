package net.watchbox.domain.box.dto.response.Invitation;

import lombok.Data;
import net.watchbox.domain.box.entity.request.InviteBoxRequest;
import net.watchbox.domain.box.entity.request.RequestStatus;

@Data
public class InvitationReceivedResponse { // 받은초대 정보
    private Long requestId;
    private Long senderId;
    private String sender; // 초대 보낸사람 닉네임
    private String sharedBoxTitle;
    private RequestStatus status;

    public static InvitationReceivedResponse from(InviteBoxRequest inviteBoxRequest) {
        InvitationReceivedResponse response = new InvitationReceivedResponse();
        response.setRequestId(inviteBoxRequest.getRequestId());
        response.setSenderId(inviteBoxRequest.getSender().getMemberId());
        response.setSender(inviteBoxRequest.getSender().getNickname());
        response.setSharedBoxTitle(inviteBoxRequest.getSharedBox().getTitle());
        response.setStatus(inviteBoxRequest.getStatus());
        return response;
    }
}
