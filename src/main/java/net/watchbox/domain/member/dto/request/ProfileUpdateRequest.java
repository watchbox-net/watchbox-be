package net.watchbox.domain.member.dto.request;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.global.validation.validatator.ValidNickname;

@Getter
@ToString
public class ProfileUpdateRequest {
    @ValidNickname
    private String nickname;
}
