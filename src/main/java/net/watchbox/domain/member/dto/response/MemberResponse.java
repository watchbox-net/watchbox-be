package net.watchbox.domain.member.dto.response;

import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.member.entity.Member;

@Getter
@ToString
public class MemberResponse {
    private Long memberId;
    private String email;
    private String nickname;
    private String profileImage;

    public static MemberResponse from(Member member) {
        MemberResponse response = new MemberResponse();
        response.memberId = member.getMemberId();
        response.email = member.getEmail();
        response.nickname = member.getNickname();
        response.profileImage = member.getProfileImage();
        return response;
    }
}
