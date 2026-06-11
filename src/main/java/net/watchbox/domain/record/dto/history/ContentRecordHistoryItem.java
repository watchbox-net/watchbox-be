package net.watchbox.domain.record.dto.history;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.dto.list.ContentSummary;
import net.watchbox.domain.record.entity.history.ContentRecordHistoryEventType;
import net.watchbox.domain.record.entity.record.WatchStatus;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
public class ContentRecordHistoryItem {
    private Long contentRecordHistoryId;
    private ContentRecordHistoryEventType eventType;
    private WatchStatus oldStatus; // WATCH_STATUS_CHANGED 일 때만 존재
    private WatchStatus newStatus; // WATCH_STATUS_CHANGED 일 때만 존재 (삭제 시 null)
    private ContentSummary contentSummary;
    private LocalDateTime createdAt;
}
