package net.watchbox.global.dto.response.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    /**
     * Content, Movie, TV, Person
     */
    // Content 오류
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT-404", "Content를 찾을 수 없습니다."),

    // Movie 오류
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "MOVIE-404", "Movie를 찾을 수 없습니다."),

    // TV 오류
    TV_NOT_FOUND(HttpStatus.NOT_FOUND, "TV-404", "TV를 찾을 수 없습니다."),

    // Person 오류
    PERSON_NOT_FOUND(HttpStatus.NOT_FOUND, "PERSON-404", "Person을 찾을 수 없습니다."),

    UNSUPPORTED_MEDIA_TYPE(HttpStatus.BAD_REQUEST, "CONTENT-400", "지원하지 않는 MediaType 입니다."),
    /**
     * Box
     */
    // MyBox 오류
    CONTENT_NOT_IN_MY_BOX(HttpStatus.NOT_FOUND, "MY-BOX-404", "내 박스에 추가된 컨텐츠가 아닙니다."),
    CONTENT_ALREADY_IN_MY_BOX(HttpStatus.CONFLICT, "MY-BOX-409", "이미 내 박스에 추가된 컨텐츠입니다."),

    // SharedBox 오류
    SHARED_BOX_NOT_FOUND(HttpStatus.NOT_FOUND, "SHARED-BOX-404", "요청한 공유 박스를 찾을 수 없습니다."),
    CONTENT_ALREADY_IN_SHARED_BOX(HttpStatus.CONFLICT, "SHARED-BOX-409", "이미 해당 멤버가 공유 박스에 추가한 컨텐츠입니다."),

    // BoxMember 오류
    BOX_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "BOX-MEMBER-404", "BoxMember를 찾을 수 없습니다."),
    FORBIDDEN_BOX_ACCESS(HttpStatus.FORBIDDEN, "BOX-MEMBER-403", "박스 접근 권한이 없습니다."),

    // SharedBoxContent 오류
    SHARED_BOX_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SHARED-BOX-CONTENT-404", "SharedBoxContent를 찾을 수 없습니다."),

    /**
     * TMDB API
     */
    // TMDB 검색 오류
    TMDB_SEARCH_BAD_REQUEST(HttpStatus.BAD_REQUEST, "TMDB-SEARCH-400", "TMDB 검색 잘못된 요청"),
    TMDB_SEARCH_BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "TMDB-SEARCH-502", "TMDB 검색 응답 오류"),
    TMDB_SEARCH_NULL_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "TMDB-SEARCH-500", "TMDB 검색 응답 null"),

    /**
     * Member
     */
    // Member 오류
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-404", "Member를 찾을 수 없습니다."),
    REQUEST_UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "MEMBER-401", "요청 권한이 없는 사용자입니다."),

    // 체크 리스트 전달 관련 오류
    EMPTY_CHECKED_LIST(HttpStatus.BAD_REQUEST, "CHECKED-LIST-400", "빈 배열은 허용되지 않습니다.");



    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
