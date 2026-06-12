package net.watchbox.domain.box.entity.history;

/**
 * 박스 히스토리 이벤트 종류.
 *
 * <p>각 타입별로 사용하는 선택 필드가 다름:
 * <ul>
 *   <li>CONTENT_ADDED / CONTENT_DELETED → content</li>
 *   <li>MEMBER_JOINED / MEMBER_LEFT / MEMBER_KICKED → targetMember</li>
 *   <li>BOX_NAME_CHANGED / BOX_DESCRIPTION_CHANGED → oldValue, newValue</li>
 * </ul>
 */
public enum BoxHistoryEventType {
    // ────── 구현됨 ──────
    CONTENT_ADDED,
    CONTENT_DELETED,
    MEMBER_JOINED,

    // ────── 미구현 ──────
    MEMBER_LEFT,
    MEMBER_KICKED,
    MEMBER_NAME_CHANGED,
    BOX_NAME_CHANGED,
    BOX_DESCRIPTION_CHANGED
}
