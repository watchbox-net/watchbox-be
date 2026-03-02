package net.watchbox.domain.member.dto.response;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MemberStatsResponse {
    private int likeCount;
    private int boxCount;
    private int recordCount;
    private int followerCount;
    private int followingCount;
}
