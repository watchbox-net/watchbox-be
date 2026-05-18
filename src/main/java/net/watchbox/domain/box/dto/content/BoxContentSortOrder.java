package net.watchbox.domain.box.dto.content;

public enum BoxContentSortOrder {
    RECENT_SAVED, OLDEST_SAVED, // createdAt 기준
    RECENT_YEAR, OLDEST_YEAR // SubContent 연도 기준
}
