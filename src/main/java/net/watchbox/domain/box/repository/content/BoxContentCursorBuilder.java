package net.watchbox.domain.box.repository.content;

import lombok.NoArgsConstructor;
import net.watchbox.domain.box.dto.content.BoxContentRecordQueryRequest;
import net.watchbox.domain.box.dto.content.BoxContentSortOrder;
import net.watchbox.domain.box.dto.content.ContentMediaTypeFilter;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.global.dto.CursorPayload;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * BoxContent 마지막 row → CursorPayload 직렬화.
 * {@link BoxContentQueryRepository cursorCondition} (역방향) 과 짝.
 * 정렬 종류별 cursor 구조가 일치해야 하므로 같은 패키지에서 관리.
 */
@NoArgsConstructor
public final class BoxContentCursorBuilder {

    /**
     * 마지막 row 의 정렬 키 값 추출 → CursorPayload 생성.
     * 정렬 종류에 따라 필요한 필드만 채움.
     * PERSON 필터 + YEAR 정렬은 RECENT_SAVED 로 fallback 되므로 SAVED 와 동일 처리.
     */
    public static CursorPayload build(BoxContent last, BoxContentRecordQueryRequest request) {
        BoxContentSortOrder sort = request.getSort();
        ContentMediaTypeFilter filter = request.getContentMediaTypeFilter();
        Long id = last.getBoxContentId();
        LocalDateTime createdAt = last.getCreatedAt();

        // PERSON + YEAR 은 SAVED 로 fallback 되었으므로 (createdAt, id) 만 사용
        if (filter == ContentMediaTypeFilter.PERSON
                && (sort == BoxContentSortOrder.RECENT_YEAR || sort == BoxContentSortOrder.OLDEST_YEAR)) {
            return CursorPayload.of(createdAt, id);
        }

        return switch (sort) {
            case RECENT_SAVED, OLDEST_SAVED -> CursorPayload.of(createdAt, id);
            case RECENT_YEAR, OLDEST_YEAR -> {
                LocalDate date = extractDate(last, filter);
                yield CursorPayload.of(date, createdAt, id);
            }
        };
    }

    /**
     * Movie.releaseDate / Tv.firstAirDate 중 해당 row 의 값 추출.
     */
    private static LocalDate extractDate(BoxContent boxContent, ContentMediaTypeFilter filter) {
        Content content = boxContent.getContent();
        if (filter == ContentMediaTypeFilter.MOVIE) {
            return content.getMovie() != null ? content.getMovie().getReleaseDate() : null;
        }
        if (filter == ContentMediaTypeFilter.TV) {
            return content.getTv() != null ? content.getTv().getFirstAirDate() : null;
        }
        // MOVIE_TV: 해당 row 의 mediaType 에 맞춰 선택
        if (content.getMovie() != null) return content.getMovie().getReleaseDate();
        if (content.getTv() != null) return content.getTv().getFirstAirDate();
        return null;
    }
}
