package net.watchbox.domain.content.facade;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.content.dto.box.ContentBoxItem;
import net.watchbox.domain.content.dto.box.ContentBoxSheetResponse;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.service.ContentCommandService;
import net.watchbox.domain.content.service.ContentQueryService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ContentBoxFacade {
    private final ContentQueryService contentQueryService;
    private final ContentCommandService contentCommandService;
    private final BoxService boxService;
    private final BoxContentQueryService boxContentQueryService;

    public ContentBoxSheetResponse getContentBoxSheet(Member member, Long contentId, MediaType mediaType) {
        List<Box> boxList = boxService.getAllBoxesByMember(member);
        Map<Long, List<String>> posterMap = boxContentQueryService.getRecentPosterPathsByBoxes(boxList);

        /*
        content가 DB에 저장되어 있는지부터 체크
        있으면 contentId로 모든 box 조회해서 포함 여부 체크,
        없으면 boxContent에도 전부 미포함이니 빈 리스트로 hasContent = false
        */
        boolean isContentSaved = contentQueryService.isContentSaved(contentId, mediaType);
        List<Long> boxIdsWithContent = isContentSaved
                ? boxContentQueryService.getBoxIdsContainingContent(contentId)
                : Collections.emptyList();

        List<ContentBoxItem> contentBoxItemList = boxList.stream()
                .map(box -> ContentBoxItem.of(box,
                        posterMap.getOrDefault(box.getBoxId(), Collections.emptyList()),
                        boxIdsWithContent.contains(box.getBoxId())))
                .toList();

        return ContentBoxSheetResponse.builder()
                .contentBoxItemList(contentBoxItemList)
                .totalCount((long) contentBoxItemList.size())
                .build();
    }

    // contentbox 넣을때도 content getOrSave 동작
}
