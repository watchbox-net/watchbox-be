package net.watchbox.domain.record.entity.history;

/**
 * 콘텐츠 시청 기록 히스토리 이벤트 종류.
 *
 * <p>타입별 사용 필드:
 * <ul>
 *   <li>WATCH_STATUS_CHANGED → oldStatus, newStatus</li>
 *   <li>LIKE_ADDED / LIKE_REMOVED → 상태 필드 없음 (member + content 만으로 사건 표현)</li>
 * </ul>
 */
public enum ContentRecordHistoryEventType {
    WATCH_STATUS_REGISTERED, // 시청 상태가 처음 등록됨 (oldStatus 없음)
    WATCH_STATUS_CHANGED,
    LIKE_ADDED,
    LIKE_REMOVED
}
