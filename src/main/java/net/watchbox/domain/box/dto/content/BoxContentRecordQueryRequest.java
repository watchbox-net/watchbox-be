package net.watchbox.domain.box.dto.content;

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
public class BoxContentRecordQueryRequest {
    @Schema(description = "정렬 기준", defaultValue = "RECENT_SAVED")
    private SortOrder sort = SortOrder.RECENT_SAVED;

    @Schema(description = "시청 상태", defaultValue = "ALL")
    private WatchStatusFilter watchStatusFilter = WatchStatusFilter.ALL;
}
