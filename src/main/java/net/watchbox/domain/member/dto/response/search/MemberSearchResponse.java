package net.watchbox.domain.member.dto.response.search;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class MemberSearchResponse {
    private Long memberId;
    private String nickname;
    private String profileImage;

    private BoxInviteStatus boxInviteStatus; // 박스 멤버 여부
//    private RequestStatus following; // 팔로잉 여부

}
