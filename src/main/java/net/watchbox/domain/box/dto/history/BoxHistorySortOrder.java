package net.watchbox.domain.box.dto.history;

public enum BoxHistorySortOrder {
    RECENT, // boxHistoryId DESC (createdAt 과 단조 증가)
    OLDEST  // boxHistoryId ASC
}
