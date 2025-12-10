package net.watchbox.global.dto.response.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Content 오류
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT-404", "Content를 찾을 수 없습니다."),

    // TMDB 검색 오류
    TMDB_SEARCH_BAD_REQUEST(HttpStatus.BAD_REQUEST, "TMDB-SEARCH-400", "TMDB 검색 잘못된 요청"),
    TMDB_SEARCH_BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "TMDB-SEARCH-502", "TMDB 검색 응답 오류"),
    TMDB_SEARCH_NULL_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "TMDB-SEARCH-500", "TMDB 검색 응답 null"),

    // Member 오류
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-404", "Member를 찾을 수 없습니다."),

    // BoxMember 오류
    BOX_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "BOX-MEMBER-404", "BoxMember를 찾을 수 없습니다."),

    // Shared Box 오류
    SHARED_BOX_NOT_FOUND(HttpStatus.NOT_FOUND, "SHARED-BOX-404", "요청한 공유 박스를 찾을 수 없습니다.")
    ;


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
