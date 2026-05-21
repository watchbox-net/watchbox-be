package net.watchbox.domain.record.repository;

import lombok.NoArgsConstructor;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.record.dto.request.ContentRecordQueryRequest;
import net.watchbox.domain.record.dto.type.ContentRecordSortOrder;
import net.watchbox.domain.record.dto.type.WatchMediaTypeFilter;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.global.dto.CursorPayload;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ContentRecord 마지막 row → CursorPayload 직렬화.
 * {@link ContentRecordQueryRepository cursorCondition} (역방향) 과 짝.
 * 정렬 종류별 cursor 구조가 일치해야 하므로 같은 패키지에서 관리.
 */
@NoArgsConstructor
public final class ContentRecordCursorBuilder {

    /**
     * 마지막 row 의 정렬 키 값 추출 → CursorPayload 생성.
     * 정렬 종류에 따라 필요한 필드만 채움.
     */
    public static CursorPayload build(ContentRecord last, ContentRecordQueryRequest request) {
        ContentRecordSortOrder sort = request.getSort();
        Long id = last.getContentRecordId();
        LocalDateTime modifiedAt = last.getModifiedAt();

        return switch (sort) {
            case RECENT_UPDATED, OLDEST_UPDATED -> CursorPayload.of(modifiedAt, id);
            case RECENT_YEAR, OLDEST_YEAR -> {
                LocalDate date = extractDate(last, request.getWatchMediaTypeFilter());
                yield CursorPayload.of(date, modifiedAt, id);
            }
        };
    }

    /**
     * Movie.releaseDate / Tv.firstAirDate 중 해당 row 의 값 추출.
     */
    private static LocalDate extractDate(ContentRecord record, WatchMediaTypeFilter filter) {
        Content content = record.getContent();
        if (filter == WatchMediaTypeFilter.MOVIE) {
            return content.getMovie() != null ? content.getMovie().getReleaseDate() : null;
        }
        if (filter == WatchMediaTypeFilter.TV) {
            return content.getTv() != null ? content.getTv().getFirstAirDate() : null;
        }
        // MOVIE_TV: 해당 row 의 mediaType 에 맞춰 선택
        if (content.getMovie() != null) return content.getMovie().getReleaseDate();
        if (content.getTv() != null) return content.getTv().getFirstAirDate();
        return null;
    }
}
