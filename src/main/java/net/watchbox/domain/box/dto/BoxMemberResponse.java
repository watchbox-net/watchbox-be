package net.watchbox.domain.box.dto;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.box.entity.member.BoxMemberRole;

@Getter
@ToString
public class BoxMemberResponse {
    private Long boxMemberId;
    private String boxMemberName;
    private BoxMemberRole role;
}
