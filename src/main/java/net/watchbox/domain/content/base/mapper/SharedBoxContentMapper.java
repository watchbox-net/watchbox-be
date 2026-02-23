package net.watchbox.domain.content.base.mapper;

import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.content.base.dto.list.ContentItem;
import net.watchbox.domain.content.base.dto.list.ContentSummary;
import net.watchbox.domain.content.base.dto.interaction.PublisherSummary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SharedBoxContentMapper {

    // === BoxContent만 ===
    public List<ContentItem> toContentItems(List<BoxContent> boxContents) {
        return boxContents.stream()
                .collect(Collectors.groupingBy(BoxContent::getTmdbId))  // tmdbId로 그룹핑
                .values().stream()
                .map(group -> {
                    BoxContent first = group.get(0);  // 콘텐츠 정보는 첫번째 것 사용
                    return ContentItem.builder()
                            .contentSummary(ContentSummary.fromContent(first.getContent()))
                            .boxContentId(first.getBoxContentId())
                            .memberInteraction(null)
                            .publisherSummaryList(null)
                            .build();
                })
                .toList();
    }

    // === + Publisher ===
    public List<ContentItem> toContentItemsWithPublisher(List<BoxContent> boxContents) {
        return boxContents.stream()
                .collect(Collectors.groupingBy(BoxContent::getTmdbId))  // tmdbId로 그룹핑
                .values().stream()
                .map(group -> {
                    BoxContent first = group.get(0);  // 콘텐츠 정보는 첫번째 것 사용
                    List<PublisherSummary> publisherSummaries = group.stream()
                            .map(bc -> PublisherSummary.from(bc.getPublisher()))
                            .toList();

                    return ContentItem.builder()
                            .contentSummary(ContentSummary.fromContent(first.getContent()))
                            .boxContentId(first.getBoxContentId())
                            .memberInteraction(null)
                            .publisherSummaryList(publisherSummaries)
                            .build();
                })
                .toList();
    }

    // 아래는 WatchRecord 만들고 수정해야함

//    // === + WatchStatus ===
//    public List<ContentItem> toContentItems(
//            List<BoxContent> boxContents,
//            Map<Long, WatchStatus> watchStatusMap
//    ) {
//        return boxContents.stream()
//                .map(bc -> ContentItem.builder()
//                        .contentSummary(ContentSummary.fromContent(bc.getContent()))
//                        .memberInteraction(MemberInteraction.builder()
//                                .watchStatus(watchStatusMap.get(bc.getTmdbId()))
//                                .isLiked(null)
//                                .build())
//                        .adderItem(null)
//                        .build())
//                .toList();
//    }
//
//    // === + WatchStatus + Like ===
//    public List<ContentItem> toContentItems(
//            List<BoxContent> boxContents,
//            Map<Long, WatchStatus> watchStatusMap,
//            Set<Long> likedTmdbIds
//    ) {
//        return boxContents.stream()
//                .map(bc -> ContentItem.builder()
//                        .contentSummary(ContentSummary.fromContent(bc.getContent()))
//                        .memberInteraction(MemberInteraction.builder()
//                                .watchStatus(watchStatusMap.get(bc.getTmdbId()))
//                                .isLiked(likedTmdbIds.contains(bc.getTmdbId()))
//                                .build())
//                        .adderItem(null)
//                        .build())
//                .toList();
//    }
//
//    // === 전부 ===
//    public List<ContentItem> toContentItems(
//            List<BoxContent> boxContents,
//            Map<Long, WatchStatus> watchStatusMap,
//            Set<Long> likedTmdbIds,
//            Map<Long, Member> addedByMap
//    ) {
//        return boxContents.stream()
//                .map(bc -> ContentItem.builder()
//                        .contentSummary(ContentSummary.fromContent(bc.getContent()))
//                        .memberInteraction(MemberInteraction.builder()
//                                .watchStatus(watchStatusMap.get(bc.getTmdbId()))
//                                .isLiked(likedTmdbIds.contains(bc.getTmdbId()))
//                                .addedBy(addedByMap.get(bc.getAddedBy().getMemberId()))
//                                .build())
//                        .build())
//                .toList();
//    }
}
