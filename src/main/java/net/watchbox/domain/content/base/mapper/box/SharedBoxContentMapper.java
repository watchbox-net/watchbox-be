package net.watchbox.domain.content.base.mapper.box;

import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.content.base.dto.interaction.MemberInteraction;
import net.watchbox.domain.content.base.dto.list.ContentItem;
import net.watchbox.domain.content.base.dto.interaction.PublisherSummary;
import net.watchbox.domain.content.base.mapper.ContentMapper;
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
                            .contentSummary(ContentMapper.fromContent(first.getContent()))
                            .boxContentId(first.getBoxContentId())
                            .memberInteraction(null)
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
                            .contentSummary(ContentMapper.fromContent(first.getContent()))
                            .boxContentId(first.getBoxContentId())
                            .memberInteraction(null)
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
                    ContentRecord record = recordMap.get(first.getTmdbId());
                    MemberInteraction interaction = record != null
                            ? MemberInteraction.builder()
                            .liked(record.getLiked())
                            .watchStatus(record.getWatchStatus())
                            .build()
                            : null;
                    List<PublisherSummary> publisherSummaries = group.stream()
                            .map(bc -> PublisherSummary.from(bc.getPublisher()))
                            .toList();

                    return ContentItem.builder()
                            .contentSummary(ContentMapper.fromContent(first.getContent()))
                            .boxContentId(first.getBoxContentId())
                            .memberInteraction(interaction)
                            .publisherSummaryList(publisherSummaries)
                            .build();
                })
                .toList();
    }
}
