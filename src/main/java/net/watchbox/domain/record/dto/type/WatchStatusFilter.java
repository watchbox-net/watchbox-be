package net.watchbox.domain.record.dto.type;

import net.watchbox.domain.record.entity.record.WatchStatus;

/**
 * 시청 기록 히스토리의 시청 상태 필터.
 * ALL: 전체 (좋아요/삭제 이벤트 포함). 나머지: 해당 상태로 변경된 이벤트만(newStatus 기준).
 */
public enum WatchStatusFilter {
    ALL(null),
    COMPLETED(WatchStatus.COMPLETED),
    WATCHING(WatchStatus.WATCHING),
    PLANNED(WatchStatus.PLANNED),
    PAUSED(WatchStatus.PAUSED);

    private final WatchStatus watchStatus;

    WatchStatusFilter(WatchStatus watchStatus) {
        this.watchStatus = watchStatus;
    }

    public WatchStatus toWatchStatus() {
        return watchStatus;
    }
}
