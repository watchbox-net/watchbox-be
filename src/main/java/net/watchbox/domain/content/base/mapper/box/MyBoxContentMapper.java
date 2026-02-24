package net.watchbox.domain.content.base.mapper.box;

import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.content.base.dto.list.ContentItem;
import net.watchbox.domain.content.base.dto.list.ContentSummary;
import net.watchbox.domain.content.base.mapper.ContentMapper;
import org.springframework.stereotype.Component;

import java.util.List;

public class MyBoxContentMapper {

    // === BoxContent만 ===
    public List<ContentItem> toContentItems(List<BoxContent> boxContents) {
        return boxContents.stream()
                .map(bc -> ContentItem.builder()
                        .contentSummary(ContentMapper.fromContent(bc.getContent()))
                        .boxContentId(bc.getBoxContentId())
                        .memberInteraction(null)
                        .build())
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
}
