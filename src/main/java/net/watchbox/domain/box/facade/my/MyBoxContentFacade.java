package net.watchbox.domain.box.facade.my;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.content.BoxContentAddRequest;
import net.watchbox.domain.box.dto.content.BoxContentAddResponse;
import net.watchbox.domain.box.dto.content.BoxContentRemoveRequest;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.validation.BoxValidator;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.content.base.dto.list.ContentItem;
import net.watchbox.domain.content.base.dto.list.ContentPageResponse;
import net.watchbox.domain.content.base.entity.Content;
import net.watchbox.domain.content.base.mapper.box.MyBoxContentMapper;
import net.watchbox.domain.content.base.service.ContentCommandService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.service.ContentRecordService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyBoxContentFacade {
    private final BoxService boxService;
    private final ContentCommandService contentCommandService;
    private final BoxContentCommandService boxContentCommandService;
    private final BoxContentQueryService boxContentQueryService;
    private final BoxValidator boxValidator;
    private final ContentRecordService contentRecordService;

    // 마이 박스에 컨텐츠 추가
    @Transactional
    public BoxContentAddResponse addMyBoxContent(Member member, Long boxId, BoxContentAddRequest request) {
        Box box = boxService.getByBoxId(boxId);

        // Content 조회 or 저장
        Content content = contentCommandService.getOrSaveContentCascade(request.getContentId(), request.getMediaType());

        // 마이 박스에 이미 존재하는지 검증
        boxValidator.validateContentNotInBox(box, content);

        // 박스에 컨텐츠 추가
        BoxContent boxContent = boxContentCommandService.addContentToBox(member, box, content);

        return BoxContentAddResponse.from(boxContent);
    }

    // 마이 박스 컨텐츠 리스트 조회
    @Transactional(readOnly = true)
    public ContentPageResponse getMyBoxContents(Member member, Long boxId) {
        Box box = boxService.getByBoxId(boxId);

        // 1. BoxContent 리스트 조회 (SubContent fetch join)
        List<BoxContent> boxContents = boxContentQueryService.getMyBoxContentAllWithSubContent(box);

        if (boxContents.isEmpty()) {
            return ContentPageResponse.empty();
        }

        // 2. 해당 contentIdList로 ContentRecord 리스트 조회
        List<Long> contentIdList = boxContents.stream()
                .map(BoxContent::getTmdbId)
                .toList();

        List<ContentRecord> records = contentRecordService.getByMemberAndContentIdIn(member, contentIdList);

        // 3. Map으로 매핑
        Map<Long, ContentRecord> recordMap = records.stream()
                .collect(Collectors.toMap(cr -> cr.getContent().getTmdbId(), cr -> cr));

        // 4. ContentItem 리스트 조립
        List<ContentItem> contentItemList = MyBoxContentMapper.toContentItems(boxContents, recordMap);

        // 5. 응답
        return ContentPageResponse.builder()
                .contentItemList(contentItemList)
                .totalCount((long) contentItemList.size())
//                .totalPages()
//                .currentPage()
                .build();
    }


    // 마이 박스 컨텐츠 삭제
    @Transactional
    public void removeMyBoxContent(Long boxId, BoxContentRemoveRequest request) {
        BoxContent boxContent = boxContentQueryService.getByBoxContentId(request.getBoxContentId());
        boxContentCommandService.deleteContentFromBox(boxContent);
        log.info("Box content {} has been removed from boxId {}", boxContent, boxId);
    }

}