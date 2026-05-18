package net.watchbox.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 커서 페이지네이션의 마지막 row 위치 정보.
 * - date: YEAR 정렬 시 1차 키 (Movie.releaseDate / Tv.firstAirDate). SAVED 정렬에선 null
 * - dateTime: SAVED 정렬 1차 키 / YEAR 정렬 2차 키 (modifiedAt 또는 createdAt)
 * - id: tie-breaker (PK)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CursorPayload(
        LocalDate date,
        LocalDateTime dateTime,
        Long id
) {
    public static CursorPayload of(LocalDateTime dateTime, Long id) {
        return new CursorPayload(null, dateTime, id);
    }

    public static CursorPayload of(LocalDate date, LocalDateTime dateTime, Long id) {
        return new CursorPayload(date, dateTime, id);
    }
}
