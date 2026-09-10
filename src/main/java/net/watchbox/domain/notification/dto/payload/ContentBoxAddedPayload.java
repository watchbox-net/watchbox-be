package net.watchbox.domain.notification.dto.payload;

import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.notification.entity.NotificationType;

/**
 * 박스에 콘텐츠가 추가됐을 때 알림 페이로드.
 * 박스 타입(MY / SHARED) 무관하게 사용.
 *
 * <p>예시 메시지: "{publisher}님이 {boxName}에 〈{contentName}〉을 추가했어요"
 */
public record ContentBoxAddedPayload(
        Long boxId,                 // 박스 ID (클릭 시 박스 콘텐츠 페이지로 이동)
        String boxName,             // 박스 이름
        BoxType boxType,            // MY / SHARED — 프론트 분기/표시용
        Long contentId,             // 추가된 콘텐츠 ID (상세 페이지 라우팅용)
        Long tmdbId,                // TMDB ID (라우팅 패턴에 따라 둘 중 하나 사용)
        String mediaType,           // MOVIE / TV / PERSON
        String contentName,         // 콘텐츠명
        String posterPath,          // 포스터 경로 (알림 카드 썸네일)
        Long publisherId,           // 추가한 멤버 ID
        String publisher,           // 추가한 멤버 닉네임 스냅샷
        String publisherProfileImage
) implements NotificationPayload {

    @Override
    public NotificationType type() {
        return NotificationType.BOX_CONTENT_ADDED;
    }

    public static ContentBoxAddedPayload of(
            Content content,
            Box box,
            Member member
    ) {
        return new ContentBoxAddedPayload(
                box.getBoxId(),
                box.getName(),
                box.getBoxType(),
                content.getContentId(),
                content.getTmdbId(),
                content.getMediaType().name(),
                extractContentName(content),
                extractPosterPath(content),
                member.getMemberId(),
                member.getNickname(),
                member.getProfileImage()
        );
    }

    /**
     * 미디어 타입별 표시명 추출 (영화 제목 / TV 제목 / 인물 이름).
     */
    private static String extractContentName(Content content) {
        return switch (content.getMediaType()) {
            case MOVIE  -> content.getMovie().getTitleKo();
            case TV     -> content.getTv().getNameKo();
            case PERSON -> content.getPerson().getNameKo();
        };
    }

    /**
     * 미디어 타입별 대표 이미지 경로 추출 (영화/TV 는 포스터, 인물은 프로필).
     */
    private static String extractPosterPath(Content content) {
        return switch (content.getMediaType()) {
            case MOVIE  -> content.getMovie().getPosterPath();
            case TV     -> content.getTv().getPosterPath();
            case PERSON -> content.getPerson().getProfilePath();
        };
    }
}
