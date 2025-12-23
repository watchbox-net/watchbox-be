package net.watchbox.domain.box.facade;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.request.BoxContentRequest;
import net.watchbox.domain.box.service.BoxMemberService;
import net.watchbox.domain.box.service.content.SharedBoxContentService;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.content.common.service.ContentCommandService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SharedBoxContentFacade {
    private final ContentCommandService contentCommandService;
    private final MemberService memberService;
    private final SharedBoxContentService sharedBoxContentService;
    private final BoxMemberService boxMemberService;

    public void addSharedBoxContent(Member member, Long boxId, BoxContentRequest boxContentRequests) {
        // 1. 추가할 권한이 있는지 검증
        boxMemberService.validateSharedBoxAdder(boxId, member);

        // 2. Content 데이터 존재 여부 확인 및 저장 후 반환
        Content content = contentCommandService.getOrSaveContentCascade(boxContentRequests);

        // # 공유 박스에 이미 존재하는지 검증
        sharedBoxContentService.validateNotInSharedBox(member, content);

        // 3. 공유 박스에 컨텐츠 추가
        sharedBoxContentService.addContentToSharedBox(member, boxId, content);
    }

    public void addSharedBoxContentList(Member member, Long boxId, List<BoxContentRequest> boxContentRequests) {

    }

    public void addSharedBoxContentListFromMine(Member member, Long boxId, List<Long> contentIds) {

    }
}
