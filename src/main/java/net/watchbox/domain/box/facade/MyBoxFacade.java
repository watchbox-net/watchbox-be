package net.watchbox.domain.box.facade;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.response.MyBoxContentResponse;
import net.watchbox.domain.box.service.my.MyBoxContentService;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.content.common.entity.MediaType;
import net.watchbox.domain.content.common.service.ContentCommandService;
import net.watchbox.domain.content.common.service.ContentQueryService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MyBoxFacade {
    private final ContentQueryService contentQueryService;
    private final ContentCommandService contentCommandService;
    private final MyBoxContentService myBoxContentService;

    // 마이 박스에 컨텐츠 추가
    public void addMyBoxContent(Member member, Long tmdbId, MediaType mediaType) {
        /**
         * 1. Content 데이터 존재 여부 확인
         * - 존재하면 패스
         * - 존재하지 않으면
         *    1-1) Content 정보 저장
         *    1-2) SubContents 저장 or ToDo) 요청 이벤트 발행 요청 이벤트 발행
         */
        Optional<Content> foundContent = contentQueryService.findContentById(tmdbId);
        Content content;
        if (foundContent.isPresent()) {
            content = foundContent.get();
        } else {
            // 1) Content 정보 저장
            content = contentCommandService.createContent(tmdbId, mediaType);
            // 2) Content의 하위 엔티티 저장
            contentCommandService.createSubContents(content);
        }

        /** 2. 마이 박스에 컨텐츠 추가 */
        myBoxContentService.addContentToMyBox(member, content);
    }

    // 마이 박스 컨텐츠 리스트 조회 ToDo: 무한스크롤 QueryDSL
    public MyBoxContentResponse getMyBoxContents(Member member) {
        return myBoxContentService.getMyBoxContentsAll(member);
    }

    // 마이 박스에 컨텐츠 삭제
    public void removeMyBoxContent(Member member, Long tmdbId) {
        myBoxContentService.deleteContentFromMyBox(member, tmdbId);
    }

}
