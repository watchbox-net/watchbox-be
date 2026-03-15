package net.watchbox.domain.content.base.mapper.box;

import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.content.base.dto.interaction.MemberInteraction;
import net.watchbox.domain.content.base.dto.list.ContentItem;
import net.watchbox.domain.content.base.mapper.ContentSummaryMapper;
import net.watchbox.domain.record.entity.ContentRecord;

import java.util.List;
import java.util.Map;

public class MyBoxContentMapper {

    // === SubContent ===
    public static List<ContentItem> toContentItems(List<BoxContent> boxContents) {
        return boxContents.stream()
                .map(bc -> ContentItem.builder()
                        .contentSummary(ContentSummaryMapper.fromContent(bc.getContent()))
                        .boxContentId(bc.getBoxContentId())
                        .memberInteraction(null)
                        .build())
                .toList();
    }

    // === SubContent + Record
    public static List<ContentItem> toContentItems(List<BoxContent> boxContents, Map<Long, ContentRecord> recordMap) {
        return boxContents.stream()
                .map(bc -> ContentItem.builder()
                        .contentSummary(ContentSummaryMapper.fromContent(bc.getContent()))
                        .boxContentId(bc.getBoxContentId())
                        .memberInteraction(MemberInteraction.from(recordMap.get(bc.getTmdbId())))
                        .build())
                .toList();
    }

}
