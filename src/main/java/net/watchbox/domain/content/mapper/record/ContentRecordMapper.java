package net.watchbox.domain.content.mapper.record;

import net.watchbox.domain.content.dto.interaction.MemberRecord;
import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.mapper.ContentSummaryMapper;
import net.watchbox.domain.record.entity.record.ContentRecord;

import java.util.List;
import java.util.Set;

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

    /**
     * 박스 포함 여부를 함께 싣는다. 시청 기록 시트 전용.
     *
     * @param addedContentIds 이 박스에 담겨 있는 contentId 집합. {@code contains} 를 페이지 전체에
     *                        돌리므로 List 가 아니라 Set 으로 받는다.
     */
    public static List<ContentItem> toContentItems(List<ContentRecord> contentRecords,
                                                   Set<Long> addedContentIds) {
        return contentRecords.stream()
                .map(record -> ContentItem.builder()
                        .contentSummary(ContentSummaryMapper.fromContent(record.getContent()))
                        .memberRecord(MemberRecord.from(record))
                        .hasAddedInbox(addedContentIds.contains(record.getContent().getContentId()))
                        .build()
                )
                .toList();
    }
}
