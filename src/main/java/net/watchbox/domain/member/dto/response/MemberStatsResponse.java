package net.watchbox.domain.member.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class MemberStatsResponse {
    private Long likeCount;
    private Long boxCount;
    private Long watchStatusCount;
    private Long followerCount;
    private Long followingCount;
}
