package net.watchbox.domain.content.dto.interaction;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.entity.WatchStatus;

@Getter
@ToString
@Builder
public class MemberRecord {
    private Long recordId;
    private Boolean liked;
    private WatchStatus watchStatus;

    // 좋아요와 시청상태가 등록되지 않은 경우 각각
    // liked는 false, watchStatus는 NONE으로 응답
    public static MemberRecord from(ContentRecord record) {
        if (record == null) {
            return MemberRecord.builder()
                    .recordId(null)
                    .liked(false)
                    .watchStatus(WatchStatus.NONE)
                    .build();
        }
        return MemberRecord.builder()
                .recordId(record.getContentRecordId())
                .liked(record.getLiked() != null ? record.getLiked() : false)
                .watchStatus(record.getWatchStatus() != null ? record.getWatchStatus() : WatchStatus.NONE)
                .build();
    }
}
