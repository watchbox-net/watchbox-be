package net.watchbox.domain.record.dto.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.watchbox.domain.record.dto.type.ContentRecordHistorySortOrder;
import net.watchbox.domain.record.dto.type.WatchStatusFilter;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ContentRecordHistoryQueryRequest {
    @Schema(description = "정렬 기준 (최신순/오래된순)", defaultValue = "RECENT")
    private ContentRecordHistorySortOrder sort = ContentRecordHistorySortOrder.RECENT;

    @Schema(description = "시청 상태 필터 (전체 또는 단일 상태)", defaultValue = "ALL")
    private WatchStatusFilter watchStatusFilter = WatchStatusFilter.ALL;

    @Schema(description = "커서 - 첫 페이지는 null, 이후 응답의 nextCursor 사용")
    private String cursor;
}
