package net.watchbox.domain.box.facade.my;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.BoxContentAddRequest;
import net.watchbox.domain.box.dto.BoxContentAddResponse;
import net.watchbox.domain.box.dto.response.my.MyBoxContentListResponse;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.box.service.BoxService;
import net.watchbox.domain.box.service.BoxValidator;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.box.service.content.MyBoxContentService;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.content.common.service.ContentCommandService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyBoxContentFacade {
    private final BoxService boxService;
    private final ContentCommandService contentCommandService;
    private final BoxContentCommandService boxContentCommandService;
    private final BoxContentQueryService boxContentQueryService;
    private final BoxValidator boxValidator;

    private final MyBoxContentService myBoxContentService;



    // 마이 박스에 컨텐츠 추가
    @Transactional
    public BoxContentAddResponse addMyBoxContent(Member member, BoxContentAddRequest request) {
        Box box = boxService.findById(request.getBoxId());

        // Content DB 조회 or 저장
        Content content = contentCommandService.getOrSaveContentCascade(request);

        // 마이 박스에 이미 존재하는지 검증
        boxValidator.validateContentNotInBox(box, content);

        // 박스에 컨텐츠 추가
        BoxContent boxContent = boxContentCommandService.addContentToBox(member, box, content);

        return BoxContentAddResponse.from(boxContent);
    }

    // 마이 박스 컨텐츠 리스트 조회 ToDo: 무한스크롤 QueryDSL
    public MyBoxContentListResponse getMyBoxContents(Member member) {
        return myBoxContentService.getMyBoxContentsAll(member);
    }

    // 마이 박스 컨텐츠 삭제
    @Transactional
    public void removeMyBoxContent(Long boxContentId){
        BoxContent boxContent = boxContentQueryService.findById(boxContentId);
        boxContentCommandService.deleteContentFromBox(boxContent);
        log.info("Deleted MyBoxContent with ID: {}", boxContentId);
    }

}
