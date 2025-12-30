package net.watchbox.domain.box.facade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.request.BoxContentRequest;
import net.watchbox.domain.box.dto.response.content.SharedBoxContentResponse;
import net.watchbox.domain.box.entity.SharedBox;
import net.watchbox.domain.box.entity.content.SharedBoxContent;
import net.watchbox.domain.box.service.BoxMemberService;
import net.watchbox.domain.box.service.SharedBoxService;
import net.watchbox.domain.box.service.content.SharedBoxContentCommandService;
import net.watchbox.domain.box.service.content.SharedBoxContentQueryService;
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
    private final SharedBoxService sharedBoxService;
    private final SharedBoxContentCommandService sharedBoxContentCommandService;
    private final SharedBoxContentQueryService sharedBoxContentQueryService;
    private final BoxMemberService boxMemberService;

    @Transactional
    public void addSharedBoxContent(Member member, Long boxId, BoxContentRequest boxContentRequests) {
        SharedBox sharedBox = sharedBoxService.findById(boxId);

        // 1. 추가 권한 검증
        boxMemberService.validateSharedBoxContentAdder(sharedBox, member);

        // 2. Content 데이터 존재 여부 확인 및 저장 후 반환
        Content content = contentCommandService.getOrSaveContentCascade(boxContentRequests);

        // 공유 박스에 이미 존재하는지 검증
        sharedBoxContentCommandService.validateNotInSharedBox(member, sharedBox, content);

        // 3. 공유 박스에 컨텐츠 추가
        sharedBoxContentCommandService.addContentToSharedBox(member, sharedBox, content);

        log.info("Member ID {} added SharedBoxContent ID {} to SharedBox ID {}", member.getMemberId(), content.getTmdbId(), boxId);
    }

    @Transactional
    public void addSharedBoxContentList(Member member, Long boxId, List<BoxContentRequest> boxContentRequests) {
        SharedBox sharedBox = sharedBoxService.findById(boxId);

        // 추가 권한 검증
        boxMemberService.validateSharedBoxContentAdder(sharedBox, member);

        for(BoxContentRequest boxContentRequest : boxContentRequests) {
            // Content 데이터 존재 여부 확인 및 저장 후 반환
            Content content = contentCommandService.getOrSaveContentCascade(boxContentRequest);

            // 공유 박스에 이미 존재하면 패스
            if(sharedBoxContentCommandService.existsInSharedBox(member, sharedBox, content)) continue;

            // 공유 박스에 컨텐츠 추가
            sharedBoxContentCommandService.addContentToSharedBox(member, sharedBox, content);
        }
    }

    @Transactional
    public void addSharedBoxContentListFromMine(Member member, Long boxId, List<Long> tmdbIds) {
        SharedBox sharedBox = sharedBoxService.findById(boxId);

        // 추가 권한 검증
        boxMemberService.validateSharedBoxContentAdder(sharedBox, member);

        // TMDB ID로 Content 리스트 조회
        List<Content> contents = contentQueryService.getContentsByTmdbIds(tmdbIds);

        for(Content content : contents) {
            // 공유 박스에 이미 존재하면 패스
            if(sharedBoxContentCommandService.existsInSharedBox(member, sharedBox, content)) continue;

            // 공유 박스에 컨텐츠 추가
            sharedBoxContentCommandService.addContentToSharedBox(member, sharedBox, content);
        }
    }

    public SharedBoxContentResponse getSharedBoxContents(Member member, Long boxId) {
        SharedBox sharedBox = sharedBoxService.findById(boxId);
        return sharedBoxContentQueryService.getSharedBoxContentsAll(sharedBox, member);
    }

    @Transactional
    public void removeSharedBoxContent(Member member, Long boxId, Long sbcId) {
        SharedBox sharedBox = sharedBoxService.findById(boxId);
        SharedBoxContent sharedBoxContent = sharedBoxContentQueryService.getSharedBoxContentById(sbcId);

        // 삭제 권한 검증
        boxMemberService.validateSharedBoxContentRemover(sharedBox, member, sharedBoxContent);

        // 공유 박스 컨텐츠 삭제
        sharedBoxContentCommandService.deleteContentFromSharedBox(member, sharedBoxContent);

        log.info("Member ID {} deleted SharedBoxContent ID {} from SharedBox ID {}", member.getMemberId(), sbcId, boxId);
    }

    @Transactional
    public void removeSharedBoxContentList(Member member, Long boxId, List<Long> sbcIds) {
        SharedBox sharedBox = sharedBoxService.findById(boxId);
        List<SharedBoxContent> sharedBoxContents = sharedBoxContentQueryService.getSharedBoxContentsByIds(sbcIds);

        for(SharedBoxContent sharedBoxContent : sharedBoxContents) {
            // 삭제 권한 검증
            boxMemberService.validateSharedBoxContentRemover(sharedBox, member, sharedBoxContent);

            // 공유 박스 컨텐츠 삭제
            sharedBoxContentCommandService.deleteContentFromSharedBox(member, sharedBoxContent);

            log.info("Member ID {} deleted SharedBoxContent ID {} from SharedBox ID {}", member.getMemberId(), sharedBoxContent.getSharedBoxContentId(), boxId);
        }

    }
}
