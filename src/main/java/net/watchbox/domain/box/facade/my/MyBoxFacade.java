package net.watchbox.domain.box.facade.my;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.BoxCreateRequest;
import net.watchbox.domain.box.dto.BoxCreateResponse;
import net.watchbox.domain.box.dto.MyBoxListResponse;
import net.watchbox.domain.box.dto.MyBoxResponse;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.service.BoxMemberService;
import net.watchbox.domain.box.service.BoxService;
import net.watchbox.domain.box.service.BoxValidator;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MyBoxFacade {
    private final BoxService boxService;
    private final BoxMemberService boxMemberService;
    private final BoxValidator boxValidator;

    @Transactional
    public BoxCreateResponse createMyBox(Member member, BoxCreateRequest request) {
        Box box = boxService.createBox(request);
        boxMemberService.addOwnerToBox(member, box);
        return BoxCreateResponse.from(box, member.getMemberId());
    }

    @Transactional(readOnly = true)
    public MyBoxResponse getMyBox(Member member, Long boxId) {
        Box box = boxService.findById(boxId);
        boxValidator.validateBoxOwner(box, member);
        return MyBoxResponse.from(box);
    }

    @Transactional(readOnly = true)
    public MyBoxListResponse getMyBoxList(Member member) {
        List<Box> myBoxList = boxService.findAllMyBoxListByOwner(member);
        List<MyBoxResponse> myBoxResponseList = myBoxList.stream()
                .map(MyBoxResponse::from)
                .toList();
        return MyBoxListResponse.builder()
                .boxList(myBoxResponseList)
                .build();
    }
}
