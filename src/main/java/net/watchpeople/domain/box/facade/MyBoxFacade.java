package net.watchpeople.domain.box.facade;

import lombok.RequiredArgsConstructor;
import net.watchpeople.domain.box.service.MyContentService;
import net.watchpeople.domain.content.common.service.ContentCommandService;
import net.watchpeople.domain.content.common.service.ContentQueryService;
import net.watchpeople.domain.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyBoxFacade {
    private final MyContentService myContentService;
    private final ContentQueryService contentQueryService;
    private final ContentCommandService contentCommandService;

    // 마이 박스에 컨텐츠 추가
    public void addMyBoxContent(Member member, Long tmdbId) {
        /**
         # 동기 처리
         1. Content 데이터 존재 여부 확인
         - 존재하면 패스
         - 존재하지 않으면
            1) Content 정보 저장 - SQL Insert (native/JdbcTemplate/Flyway)
            2) Content의 하위 엔티티 저장 작업 요청 이벤트 발행
         2. MyContent or SharedContent 정보 저장

         # 비동기 처리
         이벤트 구독 - TMDB API에서 Content의 하위 엔티티 정보 조회 후 DB에 저장 (비동기)
         */
        myContentService.addContentToMyBox(member, tmdbId);
    }

    // 마이 박스 컨텐츠 리스트 조회
    // 마이 박스에 컨텐츠 삭제
}
