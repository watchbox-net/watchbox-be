package net.watchbox.domain.record.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.global.dto.request.SortOrder;

@Getter
@ToString
public class ContentRecordQueryRequest {
    @Schema(description = "정렬 기준", example = "RECENT_SAVED, RECENT_WATCHED, OLDEST_SAVED, OLDEST_WATCHED")
    private SortOrder sort = SortOrder.RECENT_SAVED;

    @Schema(description = "시청 상태", example = "ALL, COMPLETED, WATCHING, PLANNED, PAUSED, LIKED")
    private WatchRecordFilter watchRecordFilter =  WatchRecordFilter.ALL;

}
