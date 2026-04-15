package net.watchbox.domain.content.mapper.box;

import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.content.dto.interaction.MemberRecord;
import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.mapper.ContentSummaryMapper;
import net.watchbox.domain.record.entity.ContentRecord;

import java.util.List;
import java.util.Map;

public class MyBoxContentMapper {

    // === SubContent ===
    public static List<ContentItem> toContentItems(List<BoxContent> boxContents) {
        return boxContents.stream()
                .map(bc -> ContentItem.builder()
                        .contentSummary(ContentSummaryMapper.fromContent(bc.getContent()))
                        .memberRecord(null)
                        .build())
                .toList();
    }

    // === SubContent + Record
    public static List<ContentItem> toContentItems(List<BoxContent> boxContents, Map<Long, ContentRecord> recordMap) {
        return boxContents.stream()
                .map(bc -> ContentItem.builder()
                        .contentSummary(ContentSummaryMapper.fromContent(bc.getContent()))
                        .memberRecord(MemberRecord.from(recordMap.get(bc.getContent().getContentId())))
                        .build())
                .toList();
    }

}
