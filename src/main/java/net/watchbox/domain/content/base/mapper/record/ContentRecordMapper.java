package net.watchbox.domain.content.base.mapper.record;

import net.watchbox.domain.content.base.dto.interaction.MemberInteraction;
import net.watchbox.domain.content.base.dto.list.ContentItem;
import net.watchbox.domain.content.base.mapper.ContentMapper;
import net.watchbox.domain.record.entity.ContentRecord;

import java.util.List;

public class ContentRecordMapper {
    public static List<ContentItem> toContentItems(List<ContentRecord> contentRecords) {
        return contentRecords.stream()
                .map(record -> ContentItem.builder()
                        .contentSummary(ContentMapper.fromContent(record.getContent()))
                        .memberInteraction(toMemberInteraction(record))
                        .contentRecordId(record.getContentRecordId())
                        .build()
                )
                .toList();
    }

    public static MemberInteraction toMemberInteraction(ContentRecord record) {
        return MemberInteraction.builder()
                .liked(record.getLiked())
                .watchStatus(record.getWatchStatus())
                .build();
    }
}
