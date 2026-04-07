package net.watchbox.domain.box.facade.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.content.BoxContentAddRequest;
import net.watchbox.domain.box.dto.content.BoxContentAddResponse;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.service.member.BoxMemberService;
import net.watchbox.domain.box.service.validation.BoxValidator;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.dto.list.ContentPageResponse;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.mapper.box.SharedBoxContentMapper;
import net.watchbox.domain.content.service.ContentCommandService;
import net.watchbox.domain.content.service.ContentQueryService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.ContentRecord;
import net.watchbox.domain.record.service.ContentRecordQueryService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Deprecated
public class SharedBoxContentFacade {
    private final ContentCommandService contentCommandService;
    private final ContentQueryService contentQueryService;
    private final BoxService boxService;
    private final BoxContentCommandService boxContentCommandService;
    private final BoxContentQueryService boxContentQueryService;
    private final BoxMemberService boxMemberService;
    private final BoxValidator boxValidator;
    private final ContentRecordQueryService contentRecordQueryService;

    // 공유 박스에 컨텐츠 추가
    @Transactional
    public BoxContentAddResponse addSharedBoxContent(Member member, Long boxId, BoxContentAddRequest request) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);

        // 추가 권한 검증
        boxValidator.validateBoxContentAdder(box, member);

        // Content 조회 or 저장
        Content content = contentCommandService.getOrSaveContentCascade(request.getContentId(), request.getMediaType());

        // 해당 멤버로 이미 추가된 컨텐츠인지 검증
        boxValidator.validateContentNotInSharedBox(member, box, content);

        // 박스에 컨텐츠 추가
        BoxContent boxContent = boxContentCommandService.addContentToBox(member, box, content);

        // 박스 lastContentAddedAt 업데이트
        box.updateLastContentAddedAt(boxContent.getCreatedAt());

        return BoxContentAddResponse.from(boxContent);
    }

//    @Transactional
//    public void addSharedBoxContentList(Member member, Long boxId, List<BoxContentAddRequest> boxContentAddRequests) {
//        Box box = boxService.findById(boxId);
//
//        // 추가 권한 검증
//        boxValidator.validateBoxContentAdder(box, member);
//
//        for(BoxContentAddRequest boxContentAddRequest : boxContentAddRequests) {
//            // Content DB 조회 or 저장
//            Content content = contentCommandService.getOrSaveContentCascade(boxContentAddRequest);
//
//            // 공유 박스에 이미 존재하면 패스
//            if(boxValidator.contentExistsInSharedBox(member, box, content)) continue;
//
//            // 공유 박스에 컨텐츠 추가
//            boxContentCommandService.addContentToSharedBox(member, box, content);
//        }
//    }

//    @Transactional
//    public void addSharedBoxContentListFromMine(Member member, Long boxId, List<Long> tmdbIds) {
//        Box box = boxService.findById(boxId);
//
//        // 추가 권한 검증
//        boxValidator.validateBoxContentAdder(box, member);
//
//        // TMDB ID로 Content 리스트 조회
//        List<Content> contents = contentQueryService.getContentsByTmdbIds(tmdbIds);
//
//        for(Content content : contents) {
//            // 공유 박스에 이미 존재하면 패스
//            if(boxValidator.contentExistsInSharedBox(member, box, content)) continue;
//
//            // 공유 박스에 컨텐츠 추가
//            boxContentCommandService.addContentToSharedBox(member, box, content);
//        }
//    }

    // 공유 박스 컨텐츠 리스트 조회
    @Transactional(readOnly = true)
    public ContentPageResponse getSharedBoxContents(Member member, Long boxId) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);

        // 1. BoxContent 리스트 조회 (SubContent fetch join)
        List<BoxContent> boxContents = boxContentQueryService.getMyBoxContentAllWithSubContent(box);

        if (boxContents.isEmpty()) {
            return ContentPageResponse.empty();
        }

        // 2. 해당 contentIdList로 ContentRecord 리스트 조회
        List<Long> contentIdList = boxContents.stream()
                .map(BoxContent::getTmdbId)
                .toList();

        List<ContentRecord> records = contentRecordQueryService.getByMemberAndContentIdIn(member, contentIdList);

        // 3. Map으로 매핑
        Map<Long, ContentRecord> recordMap = records.stream()
                .collect(Collectors.toMap(cr -> cr.getContent().getTmdbId(), cr -> cr));


        // 4. ContentItem 리스트 조립
        List<ContentItem> contentItemList = SharedBoxContentMapper.toContentItemsWithPublisher(boxContents, recordMap);

        // 5. 응답
        return ContentPageResponse.builder()
                .contentItemList(contentItemList)
                .totalCount((long) contentItemList.size())
//                .totalPages()
//                .currentPage()
                .build();
    }

    // 공유 박스에서 내 컨텐츠 삭제
    @Transactional
    public void removeSharedBoxContent(Member member, Long boxId, Long boxContentId) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        BoxContent boxContent = boxContentQueryService.getByBoxContentId(boxContentId);
        boxValidator.validateBoxContentRemover(member, boxContent);
        boxContentCommandService.deleteContentFromBox(boxContent);
        log.info("Deleted SharedBoxContent with ID: {} from SharedBox ID: {}", boxContentId, boxId);
    }

//    @Transactional
//    public void removeSharedBoxContentList(Member member, Long boxId, List<Long> sbcIds) {
//        Box box = boxService.findById(boxId);
//        List<BoxContent> boxContents = boxContentQueryService.getSharedBoxContentsByIds(sbcIds);
//
//        for(BoxContent boxContent : boxContents) {
//            // 삭제 권한 검증
//            boxValidator.validateBoxContentRemover(box, member, boxContent);
//
//            // 공유 박스 컨텐츠 삭제
//            boxContentCommandService.deleteContentFromSharedBox(member, boxContent);
//
//            log.info("Member ID {} deleted SharedBoxContent ID {} from SharedBox ID {}", member.getMemberId(), boxContent.getBoxContentId(), boxId);
//        }
//    }
}
