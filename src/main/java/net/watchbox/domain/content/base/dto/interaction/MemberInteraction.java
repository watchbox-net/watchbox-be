package net.watchbox.domain.content.base.dto.interaction;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.entity.WatchStatus;

@Getter
@ToString
@Builder
public class MemberInteraction {
    private Boolean liked;
    private WatchStatus watchStatus;

    // 좋아요와 시청상태가 등록되지 않은 경우 각각
    // liked는 false, watchStatus는 NONE으로 응답
    public static MemberInteraction from(ContentRecord record) {
        if (record == null) {
            return MemberInteraction.builder()
                    .liked(false)
                    .watchStatus(WatchStatus.NONE)
                    .build();
        }
        return MemberInteraction.builder()
                .liked(record.getLiked() != null ? record.getLiked() : false)
                .watchStatus(record.getWatchStatus() != null ? record.getWatchStatus() : WatchStatus.NONE)
                .build();
    }
}
