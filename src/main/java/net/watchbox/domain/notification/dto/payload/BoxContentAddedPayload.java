package net.watchbox.domain.notification.dto.payload;

import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.notification.entity.NotificationType;

/**
 * 박스에 컨텐츠가 추가됐을 때 알림 페이로드.
 * 박스 타입(MY / SHARED) 무관하게 사용.
 *
 * <p>예시 메시지: "{publisherNickname}님이 {boxName}에 〈{contentTitle}〉을 추가했어요"
 */
public record BoxContentAddedPayload(
        Long boxId,                  // 박스 ID (클릭 시 박스 컨텐츠 페이지로 이동)
        String boxName,              // 박스 이름 스냅샷
        BoxType boxType,             // MY / SHARED — 프론트 분기/표시용
        Long contentId,              // 추가된 컨텐츠 ID (상세 페이지 라우팅용)
        Long contentTmdbId,          // TMDB ID (라우팅 패턴에 따라 둘 중 하나 사용)
        String mediaType,            // MOVIE / TV — 상세 페이지 분기
        String contentTitle,         // 컨텐츠 제목 스냅샷
        String contentPosterPath,    // 포스터 경로 (알림 카드 썸네일)
        Long publisherId,            // 추가한 멤버 ID
        String publisherNickname,    // 추가한 멤버 닉네임 스냅샷
        String publisherProfileImage
) implements NotificationPayload {

    @Override
    public NotificationType type() {
        return NotificationType.BOX_CONTENT_ADDED;
    }
}
