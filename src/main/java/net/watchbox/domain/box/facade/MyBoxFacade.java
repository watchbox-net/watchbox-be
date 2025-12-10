package net.watchbox.domain.box.facade;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.service.MyBoxContentService;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.content.common.entity.MediaType;
import net.watchbox.domain.content.common.service.ContentCommandService;
import net.watchbox.domain.content.common.service.ContentQueryService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.search.service.SearchContentService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MyBoxFacade {
    private final MyBoxContentService myBoxContentService;
    private final ContentQueryService contentQueryService;
    private final ContentCommandService contentCommandService;
    private final SearchContentService searchContentService;

    // 마이 박스에 컨텐츠 추가
    public void addMyBoxContent(Member member, Long tmdbId, MediaType mediaType) {
        /**
         * 1. Content 데이터 존재 여부 확인
         * - 존재하면 패스
         * - 존재하지 않으면
         *    1-1) Content 정보 저장 - SQL Insert (native/JdbcTemplate/Flyway)
         *    1-2) Content의 하위 엔티티 저장 or ToDo) 요청 이벤트 발행 요청 이벤트 발행
         */
        Optional<Content> foundContent = contentQueryService.findContentById(tmdbId);
//        Content content = foundContent.orElseGet(() -> contentCommandService.createContentByTmdbId(tmdbId));
        Content content;
        if (foundContent.isPresent()) {
            content = foundContent.get();
        } else {
            // 1) Content 정보 저장 - SQL Insert
            content = contentCommandService.createContent(tmdbId, mediaType);
            // 2) Content의 하위 엔티티 저장
            // TMDB API 상세 검색으로 TmdbSearchResponseDto 호출
            // TmdbSearchResponseDto에서 가공하여 SubContent 저장
            contentCommandService.createSubContent(content);
        }

        // 3. 마이 박스에 컨텐츠 추가
        myBoxContentService.addContentToMyBox(content, member);
    }

    // 마이 박스 컨텐츠 리스트 조회
    // 마이 박스에 컨텐츠 삭제
}
