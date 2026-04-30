package net.watchbox.domain.record.dto.request;

import net.watchbox.domain.record.entity.WatchStatus;

public enum WatchRecordFilter {
    ALL(null),
    COMPLETED(WatchStatus.COMPLETED),
    WATCHING(WatchStatus.WATCHING),
    PLANNED(WatchStatus.PLANNED),
    PAUSED(WatchStatus.PAUSED),
    LIKED(null);

    private final WatchStatus watchStatus;

    WatchRecordFilter(WatchStatus watchStatus) {
        this.watchStatus = watchStatus;
    }

    public WatchStatus toWatchStatus() {
        return watchStatus;
    }
}
