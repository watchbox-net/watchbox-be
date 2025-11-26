package net.watchpeople.global.dto.response.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // TMDB 검색 관련 오류
    TMDB_SEARCH_BAD_REQUEST(HttpStatus.BAD_REQUEST, "TMDB-SEARCH-400", "TMDB 검색 잘못된 요청"),
    TMDB_SEARCH_BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "TMDB-SEARCH-502", "TMDB 검색 응답 오류"),
    TMDB_SEARCH_NULL_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "TMDB-SEARCH-500", "TMDB 검색 응답 null");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
