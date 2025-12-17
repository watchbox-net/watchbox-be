package net.watchbox.domain.box.dto.response;

import lombok.Data;
import net.watchbox.domain.box.entity.request.InviteBoxRequest;
import net.watchbox.domain.member.entity.Member;

@Data
public class InviteBoxRequestResponse {
    private Long requestId;
    private Long senderId;
    private String senderNickname;
    // ToDo: 상대 이미지, 장르 등등 추가

    public static InviteBoxRequestResponse from(InviteBoxRequest inviteBoxRequest) {
        InviteBoxRequestResponse response = new InviteBoxRequestResponse();
        Member member = inviteBoxRequest.getSender();
        response.setRequestId(inviteBoxRequest.getRequestId());
        response.setSenderId(member.getMemberId());
        response.setSenderNickname(member.getNickname());
        return response;
    }
}
