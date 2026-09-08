package net.watchbox.domain.box.dto.content.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.watchbox.domain.box.dto.content.type.ContentMediaTypeFilter;
import net.watchbox.domain.box.dto.content.type.WatchStatusFilter;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BoxContentCountRequest {
    @Schema(description = "콘텐츠 미디어 타입", defaultValue = "MOVIE_TV")
    private ContentMediaTypeFilter contentMediaTypeFilter = ContentMediaTypeFilter.MOVIE_TV;

    @Schema(description = "시청 상태", defaultValue = "ALL")
    private WatchStatusFilter watchStatusFilter = WatchStatusFilter.ALL;
}
