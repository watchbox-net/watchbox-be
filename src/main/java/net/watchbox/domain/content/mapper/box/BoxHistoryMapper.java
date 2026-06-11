package net.watchbox.domain.content.mapper.box;

import net.watchbox.domain.box.dto.history.BoxHistoryItem;
import net.watchbox.domain.box.dto.history.BoxHistoryMemberSummary;
import net.watchbox.domain.box.entity.history.BoxHistory;
import net.watchbox.domain.content.mapper.ContentSummaryMapper;

import java.util.List;

public class BoxHistoryMapper {
    public static List<BoxHistoryItem> toItems(List<BoxHistory> histories) {
        return histories.stream()
                .map(h -> BoxHistoryItem.builder()
                        .boxHistoryId(h.getBoxHistoryId())
                        .eventType(h.getEventType())
                        .actor(BoxHistoryMemberSummary.from(h.getActor()))
                        .targetMember(BoxHistoryMemberSummary.from(h.getTargetMember()))
                        .content(h.getContent() != null ? ContentSummaryMapper.fromContent(h.getContent()) : null)
                        .oldValue(h.getOldValue())
                        .newValue(h.getNewValue())
                        .createdAt(h.getCreatedAt())
                        .build()
                )
                .toList();
    }
}
