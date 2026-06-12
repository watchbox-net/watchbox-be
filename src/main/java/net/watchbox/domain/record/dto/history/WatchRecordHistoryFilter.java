package net.watchbox.domain.record.dto.history;

import net.watchbox.domain.record.entity.record.WatchStatus;

/**
 * 시청 기록 히스토리의 시청 상태 필터.
 * ALL: 전체 (좋아요/삭제 이벤트 포함).
 * LIKED: 좋아요 추가(LIKE_ADDED) 이벤트만.
 * 나머지: 해당 상태로 변경된 이벤트만(newStatus 기준).
 *
 * WatchRecordFilter와 유사하지만, 추후 유지보수 고려하여 별도 enum으로 분리.
 */
public enum WatchRecordHistoryFilter {
    ALL(null),
    COMPLETED(WatchStatus.COMPLETED),
    WATCHING(WatchStatus.WATCHING),
    PLANNED(WatchStatus.PLANNED),
    PAUSED(WatchStatus.PAUSED),
    LIKED(null);

    private final WatchStatus watchStatus;

    WatchRecordHistoryFilter(WatchStatus watchStatus) {
        this.watchStatus = watchStatus;
    }

    public WatchStatus toWatchStatus() {
        return watchStatus;
    }
}
