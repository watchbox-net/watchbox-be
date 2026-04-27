package net.watchbox.domain.record.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.watchbox.global.dto.request.SortOrder;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ContentRecordQueryRequest {
    @Schema(description = "시청 미디어 타입", defaultValue = "MOVIE_TV")
    private WatchMediaTypeFilter watchMediaTypeFilter = WatchMediaTypeFilter.MOVIE_TV;

    @Schema(description = "정렬 기준", defaultValue = "RECENT_SAVED")
    private SortOrder sort = SortOrder.RECENT_SAVED;

    @Schema(description = "시청 기록", defaultValue = "ALL")
    private WatchRecordFilter watchRecordFilter =  WatchRecordFilter.ALL;
}
