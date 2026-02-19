package net.watchbox.domain.box.facade.my;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.request.BoxContentRequest;
import net.watchbox.domain.box.dto.response.my.MyBoxContentResponse;
import net.watchbox.domain.box.service.content.MyBoxContentService;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.content.common.service.ContentCommandService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyBoxContentFacade {
    private final ContentCommandService contentCommandService;
    private final MyBoxContentService myBoxContentService;

    // 마이 박스에 컨텐츠 추가
    @Transactional
    public void addMyBoxContent(Member member, BoxContentRequest boxContentRequests) {
        // 1. Content DB 조회 or 저장
        Content content = contentCommandService.getOrSaveContentCascade(boxContentRequests);

        // # 마이 박스에 이미 존재하는지 검증
        myBoxContentService.validateNotInMyBox(member, content);

        // 2. 마이 박스에 컨텐츠 추가
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
