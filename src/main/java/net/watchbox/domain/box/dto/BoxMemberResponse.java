package net.watchbox.domain.box.dto;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.member.BoxMemberRole;

@Getter
@ToString
public class BoxMemberResponse {
    private Long boxMemberId;
    private String boxMemberName;
    private BoxMemberRole role;

    public static BoxMemberResponse from(BoxMember boxMember) {
        BoxMemberResponse boxMemberResponse = new BoxMemberResponse();
        boxMemberResponse.boxMemberId = boxMember.getBoxMemberId();
        boxMemberResponse.boxMemberName = boxMember.getMember().getNickname();
        boxMemberResponse.role = boxMember.getRole();
        return boxMemberResponse;
    }
}
