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
import net.watchbox.domain.content.base.mapper.MyBoxContentMapper;
import net.watchbox.domain.content.base.service.ContentCommandService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyBoxContentFacade {
    private final BoxService boxService;
    private final ContentCommandService contentCommandService;
    private final BoxContentCommandService boxContentCommandService;
    private final BoxContentQueryService boxContentQueryService;
    private final BoxValidator boxValidator;
    private final MyBoxContentMapper myBoxContentMapper;

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

        // 1. BoxContent 리스트 조회
        List<BoxContent> boxContents = boxContentQueryService.getMyBoxContentAllWithSubContent(box);

        if (boxContents.isEmpty()) {
            return ContentPageResponse.empty();
        }

//        // 2. 필요한 ID들 추출
//        List<Long> tmdbIdList = boxContents.stream()
//                .map(BoxContent::getTmdbId)
//                .toList();
//
//        // 3. 부가 정보 일괄 조회 (각각 IN 쿼리 1번씩)
//        Map<Long, WatchStatus> watchStatusMap =
//                watchRecordService.getWatchStatusMap(member, tmdbIds);
//
//        Set<Long> likedTmdbIds =
//                contentLikeService.getLikedTmdbIds(member, tmdbIds);

        // 4. ContentItem 리스트 조립
        List<ContentItem> contentItemList = myBoxContentMapper.toContentItems(boxContents);

        // 5. 응답
        return ContentPageResponse.builder()
                .contentItemList(contentItemList)
                .totalCount(contentItemList.size())
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