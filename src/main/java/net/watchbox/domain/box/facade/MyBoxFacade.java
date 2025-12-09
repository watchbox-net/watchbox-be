package net.watchbox.domain.box.facade;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.service.MyBoxContentService;
import net.watchbox.domain.content.common.service.ContentCommandService;
import net.watchbox.domain.content.common.service.ContentQueryService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyBoxFacade {
    private final MyBoxContentService myBoxContentService;
    private final ContentQueryService contentQueryService;
    private final ContentCommandService contentCommandService;

    // 마이 박스에 컨텐츠 추가
    public void addMyBoxContent(Member member, Long tmdbId) {
        myBoxContentService.addContentToMyBox(member, tmdbId);
    }

    // 마이 박스 컨텐츠 리스트 조회
    // 마이 박스에 컨텐츠 삭제
}
