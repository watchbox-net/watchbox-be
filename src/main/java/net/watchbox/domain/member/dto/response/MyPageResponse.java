package net.watchbox.domain.member.dto.response;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MyPageResponse {
    private ProfileResponse profile;
    private MemberStatsResponse memberStats;

    public static MyPageResponse of(ProfileResponse profile, MemberStatsResponse memberStats) {
        MyPageResponse response = new MyPageResponse();
        response.profile = profile;
        response.memberStats = memberStats;
        return response;
    }
}
