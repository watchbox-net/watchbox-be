package net.watchbox.domain.content.mapper.record;

import net.watchbox.domain.content.dto.interaction.MemberRecord;
import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.mapper.ContentSummaryMapper;
import net.watchbox.domain.record.entity.ContentRecord;

import java.util.List;

public class ContentRecordMapper {
    public static List<ContentItem> toContentItems(List<ContentRecord> contentRecords) {
        return contentRecords.stream()
                .map(record -> ContentItem.builder()
                        .contentSummary(ContentSummaryMapper.fromContent(record.getContent()))
                        .memberRecord(MemberRecord.from(record))
                        .build()
                )
                .toList();
    }
}
