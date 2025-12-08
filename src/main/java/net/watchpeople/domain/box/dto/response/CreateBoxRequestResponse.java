package net.watchpeople.domain.box.dto.response;

import lombok.Data;
import net.watchpeople.domain.box.entity.request.CreateBoxRequest;
import net.watchpeople.domain.member.entity.Member;

@Data
public class CreateBoxRequestResponse {
    private Long requestId;
    private Long senderId;
    private String senderNickname;
    // ToDo: 상대 이미지, 장르 등등 추가

    public static CreateBoxRequestResponse from(CreateBoxRequest createBoxRequest) {
        CreateBoxRequestResponse response = new CreateBoxRequestResponse();
        Member member = createBoxRequest.getSender();
        response.setRequestId(createBoxRequest.getRequestId());
        response.setSenderId(member.getMemberId());
        response.setSenderNickname(member.getNickname());
        return response;
    }
}
