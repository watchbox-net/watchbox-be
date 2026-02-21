package net.watchbox.domain.box.facade.shared;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.BoxContentAddRequest;
import net.watchbox.domain.box.dto.BoxContentAddResponse;
import net.watchbox.domain.box.dto.response.content.SharedBoxContentResponse;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.service.BoxMemberService;
import net.watchbox.domain.box.service.BoxValidator;
import net.watchbox.domain.box.service.BoxService;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.content.common.service.ContentCommandService;
import net.watchbox.domain.content.common.service.ContentQueryService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SharedBoxContentFacade {
    private final ContentCommandService contentCommandService;
    private final ContentQueryService contentQueryService;
    private final BoxService boxService;
    private final BoxContentCommandService boxContentCommandService;
    private final BoxContentQueryService boxContentQueryService;
    private final BoxMemberService boxMemberService;
    private final BoxValidator boxValidator;

    @Transactional
    public BoxContentAddResponse addSharedBoxContent(Member member, BoxContentAddRequest request) {
        Box box = boxService.findById(request.getBoxId());

        // 추가 권한 검증
        boxValidator.validateBoxContentAdder(box, member);

        // Content DB 조회 or 저장
        Content content = contentCommandService.getOrSaveContentCascade(request);

        // 해당 멤버로 이미 추가된 컨텐츠인지 검증
        boxValidator.validateContentNotInSharedBox(member, box, content);

        // 박스에 컨텐츠 추가
        BoxContent boxContent = boxContentCommandService.addContentToBox(member, box, content);

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

    public SharedBoxContentResponse getSharedBoxContents(Member member, Long boxId) {
        Box box = boxService.findById(boxId);
        return boxContentQueryService.getSharedBoxContentsAll(box, member);
    }

    @Transactional
    public void removeSharedBoxContent(Member member, Long boxContentId) {
        BoxContent boxContent = boxContentQueryService.findById(boxContentId);
        boxValidator.validateBoxContentRemover(member, boxContent);
        boxContentCommandService.deleteContentFromBox(boxContent);
        log.info("Deleted SharedBoxContent with ID: {} from SharedBox ID: {}", boxContentId, boxContent.getBox().getBoxId());
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
