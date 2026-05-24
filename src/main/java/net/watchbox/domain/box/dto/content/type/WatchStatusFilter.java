package net.watchbox.domain.box.dto.content.type;

import net.watchbox.domain.record.entity.WatchStatus;

public enum WatchStatusFilter {
    ALL(null),
    COMPLETED(WatchStatus.COMPLETED),
    WATCHING(WatchStatus.WATCHING),
    PLANNED(WatchStatus.PLANNED),
    PAUSED(WatchStatus.PAUSED),
    NONE(null);

    private final WatchStatus watchStatus;

    WatchStatusFilter(WatchStatus watchStatus) {
        this.watchStatus = watchStatus;
    }

    public WatchStatus toWatchStatus() {
        return watchStatus;
    }
}
