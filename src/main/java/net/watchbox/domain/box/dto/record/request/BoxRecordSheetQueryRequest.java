package net.watchbox.domain.box.dto.record.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.watchbox.domain.record.dto.record.request.ContentRecordQueryRequest;
import net.watchbox.domain.record.dto.type.ContentRecordSortOrder;
import net.watchbox.domain.record.dto.type.WatchMediaTypeFilter;
import net.watchbox.domain.record.dto.type.WatchRecordFilter;

/**
 * 시청 기록 시트 조회 조건.
 *
 * <p>시청 기록 페이지({@link ContentRecordQueryRequest})와 같은 커서·필터 규칙을 쓰되
 * <b>정렬은 최근 기록순으로 고정</b>한다. 이 화면은 "박스에 담을 것을 고르는" 용도라
 * 정렬 선택지를 주면 체크 상태를 유지한 채 목록이 재배치되어 혼란만 커진다.
 *
 * <p>정렬을 고정했으므로 커서 구조도 {@code (modifiedAt, contentRecordId)} 하나로 고정된다.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class BoxRecordSheetQueryRequest {

    @Schema(description = "미디어 타입 탭 (전체 / 영화 / 시리즈)", defaultValue = "MOVIE_TV")
    private WatchMediaTypeFilter watchMediaTypeFilter = WatchMediaTypeFilter.MOVIE_TV;

    @Schema(description = "시청 상태 필터", defaultValue = "ALL")
    private WatchRecordFilter watchRecordFilter = WatchRecordFilter.ALL;

    @Schema(description = "커서 - 첫 페이지는 null, 이후 응답의 nextCursor 사용")
    private String cursor;

    /**
     * 시청 기록 조회 쿼리로 변환한다. 정렬만 고정값을 끼워 넣고 나머지는 그대로 넘긴다.
     *
     * <p>조회·커서 생성이 <b>같은 request 인스턴스</b>를 봐야 정렬 키와 커서 구조가 어긋나지 않는다.
     */
    public ContentRecordQueryRequest toContentRecordQuery() {
        ContentRecordQueryRequest query = new ContentRecordQueryRequest();
        query.setWatchMediaTypeFilter(watchMediaTypeFilter);
        query.setWatchRecordFilter(watchRecordFilter);
        query.setSort(ContentRecordSortOrder.RECENT_UPDATED);
        query.setCursor(cursor);
        return query;
    }
}
