package net.watchbox.domain.content.mapper.record;

import net.watchbox.domain.content.mapper.ContentSummaryMapper;
import net.watchbox.domain.record.dto.history.ContentRecordHistoryItem;
import net.watchbox.domain.record.entity.history.ContentRecordHistory;

import java.util.List;

public class ContentRecordHistoryMapper {
    public static List<ContentRecordHistoryItem> toItems(List<ContentRecordHistory> histories) {
        return histories.stream()
                .map(history -> ContentRecordHistoryItem.builder()
                        .contentRecordHistoryId(history.getContentRecordHistoryId())
                        .eventType(history.getEventType())
                        .oldStatus(history.getOldStatus())
                        .newStatus(history.getNewStatus())
                        .contentSummary(ContentSummaryMapper.fromContent(history.getContent()))
                        .createdAt(history.getCreatedAt())
                        .build()
                )
                .toList();
    }
}
