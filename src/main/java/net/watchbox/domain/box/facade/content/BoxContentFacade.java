package net.watchbox.domain.box.facade.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.content.BoxContentAddRequest;
import net.watchbox.domain.box.dto.content.BoxContentAddResponse;
import net.watchbox.domain.box.dto.content.BoxContentCountResponse;
import net.watchbox.domain.box.dto.content.BoxContentRecordQueryRequest;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.repository.content.BoxContentCursorBuilder;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.box.service.validation.BoxValidator;
import net.watchbox.domain.content.dto.list.ContentCursorPageResponse;
import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.dto.list.ContentPageResponse;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.mapper.box.MyBoxContentMapper;
import net.watchbox.domain.content.mapper.box.SharedBoxContentMapper;
import net.watchbox.domain.content.service.ContentCommandService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.service.ContentRecordQueryService;
import net.watchbox.global.constants.AppConstants;
import net.watchbox.global.util.CursorCodec;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BoxContentFacade {
    private final BoxService boxService;
    private final ContentCommandService contentCommandService;
    private final BoxContentCommandService boxContentCommandService;
    private final BoxContentQueryService boxContentQueryService;
    private final BoxValidator boxValidator;
    private final ContentRecordQueryService contentRecordQueryService;
    private final CursorCodec cursorCodec;

    @Transactional(readOnly = true)
    public ContentCursorPageResponse getBoxContentPage(Member member, BoxContentRecordQueryRequest request, Long boxId) {
        // 1. 박스 조회 + 멤버 권한 검증
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        boxValidator.validateBoxMember(box, member);

        // 2. BoxContent 조회 (필터 + 정렬 + 커서, PAGE_SIZE+1 개)
        List<BoxContent> fetched = boxContentQueryService.getBoxContentList(box, member, request, AppConstants.PAGE_SIZE);

        if (fetched.isEmpty()) {
            return ContentCursorPageResponse.empty();
        }

        // 3. hasNext 판단 + page slice
        boolean hasNext = fetched.size() > AppConstants.PAGE_SIZE;
        List<BoxContent> page = hasNext ? fetched.subList(0, AppConstants.PAGE_SIZE) : fetched;

        // 4. 로그인 사용자의 ContentRecord 별도 조회 -> Map (contentId 기준)
        List<Long> contentIds = page.stream()
                .map(bc -> bc.getContent().getContentId())
                .distinct()
                .toList();

        Map<Long, ContentRecord> recordMap = contentRecordQueryService
                .getByMemberAndContentIds(member, contentIds)
                .stream()
                .collect(Collectors.toMap(cr -> cr.getContent().getContentId(), cr -> cr));

        // 5. 박스 타입에 따라 매퍼 분기 (공유 박스는 contentId 그룹핑 + publisherSummaryList)
        List<ContentItem> contentItemList = box.getBoxType() == BoxType.MY
                ? MyBoxContentMapper.toContentItems(page, recordMap)
                : SharedBoxContentMapper.toContentItemsWithPublisher(page, recordMap);

        // 6. nextCursor 인코딩
        String nextCursor = hasNext
                ? cursorCodec.encode(BoxContentCursorBuilder.build(page.get(page.size() - 1), request))
                : null;

        return ContentCursorPageResponse.builder()
                .contentItemList(contentItemList)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Transactional(readOnly = true)
    public BoxContentCountResponse getBoxContentCount(Member member, Long boxId) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        boxValidator.validateBoxMember(box, member);
        return BoxContentCountResponse.of(boxContentQueryService.countByBox(box));
    }

    @Transactional(readOnly = true)
    public ContentPageResponse getBoxContentPageDeprecated(Member member, Long boxId) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);

        // 1. BoxContent 리스트 조회 (SubContent fetch join)
        List<BoxContent> boxContents = boxContentQueryService.getMyBoxContentAllWithSubContent(box);

        if (boxContents.isEmpty()) {
            return ContentPageResponse.empty();
        }

        // 2. 해당 contentId로 ContentRecord 리스트 조회
        List<Long> contentIds = boxContents.stream()
                .map(bc -> bc.getContent().getContentId())
                .toList();

        List<ContentRecord> records = contentRecordQueryService
                .getByMemberAndContentIds(member, contentIds);

        // 3. Map으로 매핑 (contentId 기준)
        Map<Long, ContentRecord> recordMap = records.stream()
                .collect(Collectors.toMap(cr -> cr.getContent().getContentId(), cr -> cr));

        // 4. ContentItem 리스트 조립
        List<ContentItem> contentItemList = box.getBoxType().equals(BoxType.MY)
                ? MyBoxContentMapper.toContentItems(boxContents, recordMap)
                : SharedBoxContentMapper.toContentItemsWithPublisher(boxContents, recordMap);

        // 5. 응답
        return ContentPageResponse.builder()
                .contentItemList(contentItemList)
                .totalCount((long) contentItemList.size())
//                .totalPages()
//                .currentPage()
                .build();
    }

    @Transactional
    public BoxContentAddResponse addBoxContent(Member member, Long boxId, BoxContentAddRequest request) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);

        // 추가 권한 검증
        boxValidator.validateBoxContentAdder(box, member);

        // Content 조회 or 저장
        Content content = contentCommandService.getOrSaveContentCascade(request.getTmdbId(), request.getMediaType());

        // 박스에 이미 존재하는지 검증 -> 중복이면 예외
        if (box.getBoxType().equals(BoxType.MY)) { // 마이 박스에 이미 존재하는지 검증
            boxValidator.validateContentNotInBox(box, content);
        } else { // 해당 멤버로 이미 추가된 컨텐츠인지 검증
            boxValidator.validateContentNotInSharedBox(member, box, content);
        }

        // 박스에 컨텐츠 추가
        BoxContent boxContent = boxContentCommandService.addContentToBox(member, box, content);

        // 박스 lastContentAddedAt 업데이트
        box.updateLastContentAddedAt(boxContent.getCreatedAt());

        return BoxContentAddResponse.from(boxContent);
    }

    @Transactional
    public void removeBoxContent(Member member, Long boxId, Long boxContentId) {
        BoxType boxType = boxService.getByBoxIdOrElseThrow(boxId).getBoxType();
        BoxContent boxContent = boxContentQueryService.getByBoxContentId(boxContentId);

        // 박스 컨텐츠 추가한 사람인지 검증
        boxValidator.validateBoxContentRemover(member, boxContent);

        boxContentCommandService.deleteContentFromBox(boxContent);

        if (boxType == BoxType.MY) {
            log.info("Box content {} has been removed from boxId {}", boxContent, boxId);
        } else {
            log.info("Deleted SharedBoxContent with ID: {} from SharedBox ID: {}", boxContentId, boxId);
        }
    }


}
