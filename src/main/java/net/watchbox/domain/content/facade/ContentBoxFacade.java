package net.watchbox.domain.content.facade;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.content.dto.box.ContentBoxDiffRequest;
import net.watchbox.domain.content.dto.box.ContentBoxItem;
import net.watchbox.domain.content.dto.box.ContentBoxSheetResponse;
import net.watchbox.domain.content.dto.box.ContentBoxUpdateResponse;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.service.ContentCommandService;
import net.watchbox.domain.content.service.ContentQueryService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;

import net.watchbox.domain.content.entity.Content;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ContentBoxFacade {
    private final ContentQueryService contentQueryService;
    private final ContentCommandService contentCommandService;
    private final BoxService boxService;
    private final BoxContentQueryService boxContentQueryService;

    @Transactional(readOnly = true)
    public ContentBoxSheetResponse getContentBoxSheet(Member member, Long tmdbId, MediaType mediaType) {
        List<Box> boxList = boxService.getAllBoxesByMember(member);
        Map<Long, List<String>> posterMap = boxContentQueryService.getRecentPosterPathsByBoxes(boxList);

        /*
        Content가 DB에 있으면 contentId로 박스 포함 여부 조회,
        없으면 어떤 박스에도 없으니 빈 리스트
        */
        Optional<Content> contentOpt = contentQueryService.findByTmdbIdAndMediaType(tmdbId, mediaType);
        List<Long> boxIdsWithContent = contentOpt
                .map(content -> boxContentQueryService.getBoxIdsContainingContentById(content.getContentId()))
                .orElse(Collections.emptyList());

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

    @Transactional
    public ContentBoxUpdateResponse updateContentBoxes(Member member, Long tmdbId, MediaType mediaType, ContentBoxDiffRequest request) {
        return null;
    }

    // contentbox 넣을때도 content getOrSave 동작
}
