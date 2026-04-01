package net.watchbox.domain.content.base.mapper.box;

import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.content.base.dto.interaction.MemberRecord;
import net.watchbox.domain.content.base.dto.list.ContentItem;
import net.watchbox.domain.content.base.dto.interaction.PublisherSummary;
import net.watchbox.domain.content.base.mapper.ContentSummaryMapper;
import net.watchbox.domain.record.entity.ContentRecord;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SharedBoxContentMapper {

    // === SubContent ===
    public static List<ContentItem> toContentItems(List<BoxContent> boxContents) {
        return boxContents.stream()
                .collect(Collectors.groupingBy(BoxContent::getTmdbId))  // tmdbId로 그룹핑
                .values().stream()
                .map(group -> {
                    BoxContent first = group.getFirst();  // 콘텐츠 정보는 첫번째 것 사용
                    return ContentItem.builder()
                            .contentSummary(ContentSummaryMapper.fromContent(first.getContent()))
                            .boxContentId(first.getBoxContentId())
                            .memberRecord(null)
                            .publisherSummaryList(null)
                            .build();
                })
                .toList();
    }

    // === SubContent + Publisher ===
    public static List<ContentItem> toContentItemsWithPublisher(List<BoxContent> boxContents) {
        return boxContents.stream()
                .collect(Collectors.groupingBy(BoxContent::getTmdbId))  // tmdbId로 그룹핑
                .values().stream()
                .map(group -> {
                    BoxContent first = group.getFirst();  // 콘텐츠 정보는 첫번째 것 사용
                    List<PublisherSummary> publisherSummaries = group.stream()
                            .map(bc -> PublisherSummary.from(bc.getPublisher()))
                            .toList();

                    return ContentItem.builder()
                            .contentSummary(ContentSummaryMapper.fromContent(first.getContent()))
                            .boxContentId(first.getBoxContentId())
                            .memberRecord(null)
                            .publisherSummaryList(publisherSummaries)
                            .build();
                })
                .toList();
    }

    // === SubContent + Publisher + Record ===
    public static List<ContentItem> toContentItemsWithPublisher(List<BoxContent> boxContents, Map<Long, ContentRecord> recordMap) {
        return boxContents.stream()
                .collect(Collectors.groupingBy(BoxContent::getTmdbId))
                .values().stream()
                .map(group -> {
                    BoxContent first = group.getFirst();
                    List<PublisherSummary> publisherSummaries = group.stream()
                            .map(bc -> PublisherSummary.from(bc.getPublisher()))
                            .toList();

                    return ContentItem.builder()
                            .contentSummary(ContentSummaryMapper.fromContent(first.getContent()))
                            .boxContentId(first.getBoxContentId())
                            .memberRecord(MemberRecord.from(recordMap.get(first.getTmdbId())))
                            .publisherSummaryList(publisherSummaries)
                            .build();
                })
                .toList();
    }
}
