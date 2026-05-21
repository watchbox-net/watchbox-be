package net.watchbox.domain.box.dto.content.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.watchbox.domain.box.dto.content.type.BoxContentSortOrder;
import net.watchbox.domain.box.dto.content.type.ContentMediaTypeFilter;
import net.watchbox.domain.box.dto.content.type.WatchStatusFilter;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BoxContentQueryRequest {
    @Schema(description = "컨텐츠 미디어 타입", defaultValue = "MOVIE_TV")
    private ContentMediaTypeFilter contentMediaTypeFilter = ContentMediaTypeFilter.MOVIE_TV;

    @Schema(description = "정렬 기준", defaultValue = "RECENT_SAVED")
    private BoxContentSortOrder sort = BoxContentSortOrder.RECENT_SAVED;

    @Schema(description = "시청 상태", defaultValue = "ALL")
    private WatchStatusFilter watchStatusFilter = WatchStatusFilter.ALL;

    @Schema(description = "커서 - 첫 페이지는 null, 이후 응답의 nextCursor 사용")
    private String cursor;

//    @Schema(description = "좋아요 필터", defaultValue = "ALL")
//    private LikedFilter likedFilter = LikedFilter.ALL;
}
