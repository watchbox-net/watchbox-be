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
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.BAD_REQUEST, "CONTENT-400", "지원하지 않는 MediaType 입니다."),

    // Movie 오류
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "MOVIE-404", "Movie를 찾을 수 없습니다."),

    // TV 오류
    TV_NOT_FOUND(HttpStatus.NOT_FOUND, "TV-404", "TV를 찾을 수 없습니다."),

    // Person 오류
    PERSON_NOT_FOUND(HttpStatus.NOT_FOUND, "PERSON-404", "Person을 찾을 수 없습니다."),

    /**
     * Box
     */
    // Box 공통 오류
    BOX_NOT_FOUND(HttpStatus.NOT_FOUND, "BOX-404", "요청한 박스를 찾을 수 없습니다."),
    CANNOT_DELETE_LAST_MY_BOX(HttpStatus.CONFLICT, "BOX-409", "마지막 박스는 삭제할 수 없습니다."),
    BOX_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "BOX-CONTENT-404", "요청한 박스 컨텐츠를 찾을 수 없습니다."),
    BOX_CONTENT_ALREADY_IN_BOX(HttpStatus.CONFLICT, "BOX-CONTENT-409", "이미 박스에 추가된 컨텐츠입니다."),
    INSUFFICIENT_BOX_CONTENT_EDIT_PERMISSION(HttpStatus.FORBIDDEN, "BOX-CONTENT-403", "박스 컨텐츠 편집 권한이 없습니다."),

    // SharedBox 오류
    SHARED_BOX_NOT_FOUND(HttpStatus.NOT_FOUND, "SHARED-BOX-404", "요청한 공유 박스를 찾을 수 없습니다."),
    CONTENT_ALREADY_IN_BOX(HttpStatus.CONFLICT, "SHARED-BOX-409", "이미 해당 멤버가 박스에 추가한 컨텐츠입니다."),

    // BoxMember 오류
    BOX_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "BOX-MEMBER-404", "BoxMember를 찾을 수 없습니다."),
    NOT_BOX_MEMBER(HttpStatus.FORBIDDEN, "BOX-MEMBER-403", "해당 박스의 멤버가 아닙니다."),
    ALREADY_BOX_MEMBER(HttpStatus.CONFLICT, "BOX-MEMBER-409", "이미 박스 멤버로 존재합니다."),
    FORBIDDEN_BOX_ACCESS(HttpStatus.FORBIDDEN, "BOX-MEMBER-403", "박스에 대한 권한이 부족합니다."),

    // SharedBoxContent 오류
    FORBIDDEN_CONTENT_REMOVAL(HttpStatus.FORBIDDEN, "SHARED-BOX-CONTENT-403", "컨텐츠 삭제 권한이 없습니다."),

    // InviteBoxRequest 오류
    DUPLICATE_INVITE_REQUEST(HttpStatus.CONFLICT, "INVITE-409", "이미 진행중인 초대 요청입니다."),
    INVITATION_ALREADY_RESPONDED(HttpStatus.CONFLICT, "INVITE-409", "이미 처리된 초대 요청입니다."),

    /**
     * TMDB API
     */
    // TMDB 검색 오류
    TMDB_SEARCH_BAD_REQUEST(HttpStatus.BAD_REQUEST, "TMDB-SEARCH-400", "TMDB 검색 잘못된 요청"),
    TMDB_SEARCH_BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "TMDB-SEARCH-502", "TMDB 검색 응답 오류"),
    TMDB_SEARCH_NULL_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "TMDB-SEARCH-500", "TMDB 검색 응답 null"),

    // TMDB 공통 오류
    TMDB_RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "TMDB-404", "TMDB에서 해당 리소스를 찾을 수 없습니다."),
    TMDB_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "TMDB-4XX", "TMDB API 요청 오류"),
    TMDB_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "TMDB-5XX", "TMDB API 서버 오류"),

    /**
     * OauthAccount
     */
    NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "OauthAccount-409", "이미 존재하는 이름입니다."),

    /**
     * Member
     */
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-404", "Member를 찾을 수 없습니다."),
    REQUEST_UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "MEMBER-401", "요청 권한이 없는 사용자입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER-409", "이미 존재하는 닉네임입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER-409", "이미 존재하는 이메일입니다."),



    /**
     * ContentRecord
     */
    CONTENT_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "ContentRecord-404", "ContentRecord를 찾을 수 없습니다."),
    NOT_RECORD_MEMBER(HttpStatus.FORBIDDEN, "ContentRecord-403", "기록한 멤버가 아닙니다."),

    /**
     * Notification
     */
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION-404", "알림을 찾을 수 없거나 접근 권한이 없습니다."),

    /**
     * 인증
     */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401", "액세스 토큰이 만료되었습니다."),
    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "AUTH-401", "이미 사용된 리프레시 토큰입니다. 다시 로그인해주세요."),

    /**
     * 프론트엔드 오류
     */
    EMPTY_CHECKED_LIST(HttpStatus.BAD_REQUEST, "CHECKED-LIST-400", "빈 배열은 허용되지 않습니다."),
    PARAMETER_BAD_REQUEST(HttpStatus.BAD_REQUEST, "FE-PARAMETER-400", "잘못된 파라미터 입력입니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "CURSOR-400", "잘못된 cursor 입니다."),
    INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "IMAGE-400",
            "이미지 URL은 watchbox S3 경로(https://{bucketName}.s3.ap-northeast-2.amazonaws.com/...) 여야 합니다.");

    /**
     * 백엔드 오류
     */


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
