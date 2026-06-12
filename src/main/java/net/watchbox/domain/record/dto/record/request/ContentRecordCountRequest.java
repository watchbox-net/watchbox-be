package net.watchbox.domain.record.dto.record.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.watchbox.domain.record.dto.type.WatchMediaTypeFilter;
import net.watchbox.domain.record.dto.type.WatchRecordFilter;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ContentRecordCountRequest {
    @Schema(description = "시청 미디어 타입", defaultValue = "MOVIE_TV")
    private WatchMediaTypeFilter watchMediaTypeFilter = WatchMediaTypeFilter.MOVIE_TV;

    @Schema(description = "시청 기록", defaultValue = "ALL")
    private WatchRecordFilter watchRecordFilter =  WatchRecordFilter.ALL;
}
