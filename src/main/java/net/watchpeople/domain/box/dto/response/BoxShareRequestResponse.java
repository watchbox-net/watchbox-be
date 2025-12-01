package net.watchpeople.domain.box.dto.response;

import lombok.Data;
import net.watchpeople.domain.box.entity.SharedBoxRequest;
import net.watchpeople.domain.member.entity.Member;

@Data
public class BoxShareRequestResponse {
    private Long requestId;
    private Long senderId;
    private String senderNickname;
    // ToDo: 상대 이미지, 장르 등등 추가

    public static BoxShareRequestResponse from(SharedBoxRequest sharedBoxRequest) {
        BoxShareRequestResponse response = new BoxShareRequestResponse();
        Member member = sharedBoxRequest.getSender();
        response.setRequestId(sharedBoxRequest.getRequestId());
        response.setSenderId(member.getMemberId());
        response.setSenderNickname(member.getNickname());
        return response;
    }
}
