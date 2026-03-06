package net.watchbox.domain.member.dto.response;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.member.entity.Member;

@Getter
@ToString
public class ProfileResponse {
    private Long memberId;
    private String email;
    private String nickname;
    private String profileImage;

    public static ProfileResponse from(Member member) {
        ProfileResponse response = new ProfileResponse();
        response.memberId = member.getMemberId();
        response.email = member.getEmail();
        response.nickname = member.getNickname();
        response.profileImage = member.getProfileImage();
        return response;
    }
}
