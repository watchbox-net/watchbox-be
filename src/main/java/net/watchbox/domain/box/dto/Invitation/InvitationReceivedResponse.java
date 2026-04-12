package net.watchbox.domain.box.dto.Invitation;

import lombok.Data;
import net.watchbox.domain.box.dto.box.BoxItem;
import net.watchbox.domain.box.entity.invitation.BoxInvitation;
import net.watchbox.domain.box.entity.invitation.RequestStatus;

import java.util.List;

@Data
public class InvitationReceivedResponse { // 받은초대 정보
    private Long requestId;
    private Long senderId;
    private String sender; // 초대 보낸사람 닉네임
    private RequestStatus status;
    private BoxItem sharedBox; // 초대받은 박스 정보 (박스 제목, 설명, 포스터 3개, 멤버 리스트)

    public static InvitationReceivedResponse from(BoxInvitation boxInvitation, List<String> previewPosters) {
        InvitationReceivedResponse response = new InvitationReceivedResponse();
        response.setRequestId(boxInvitation.getRequestId());
        response.setSenderId(boxInvitation.getSender().getMemberId());
        response.setSender(boxInvitation.getSender().getNickname());
        response.setStatus(boxInvitation.getStatus());
        response.sharedBox = BoxItem.of(boxInvitation.getBox(), previewPosters);
        return response;
    }
}
