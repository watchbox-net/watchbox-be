package net.watchbox.domain.record.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.watchbox.domain.record.dto.type.ContentRecordSortOrder;
import net.watchbox.domain.record.dto.type.WatchMediaTypeFilter;
import net.watchbox.domain.record.dto.type.WatchRecordFilter;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ContentRecordQueryRequest {
    @Schema(description = "시청 미디어 타입", defaultValue = "MOVIE_TV")
    private WatchMediaTypeFilter watchMediaTypeFilter = WatchMediaTypeFilter.MOVIE_TV;

    @Schema(description = "정렬 기준", defaultValue = "RECENT_UPDATED")
    private ContentRecordSortOrder sort = ContentRecordSortOrder.RECENT_UPDATED;

    @Schema(description = "시청 기록", defaultValue = "ALL")
    private WatchRecordFilter watchRecordFilter =  WatchRecordFilter.ALL;

    @Schema(description = "커서 - 첫 페이지는 null, 이후 응답의 nextCursor 사용")
    private String cursor;
}
