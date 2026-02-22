package net.watchbox.domain.box.facade.my;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.box.*;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.service.member.BoxMemberService;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.validation.BoxValidator;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyBoxFacade {
    private final BoxService boxService;
    private final BoxMemberService boxMemberService;
    private final BoxValidator boxValidator;
    private final BoxContentCommandService boxContentCommandService;

    @Transactional
    public BoxCreateResponse createMyBox(Member member, BoxCreateRequest request) {
        Box box = boxService.createBox(member, request, BoxType.MY);
        boxMemberService.addOwnerToBox(member, box);
        return BoxCreateResponse.from(box, member.getMemberId());
    }

    @Transactional(readOnly = true)
    public MyBoxResponse getMyBox(Member member, Long boxId) {
        Box box = boxService.getByBoxId(boxId);
        boxValidator.validateBoxOwner(box, member);
        return MyBoxResponse.from(box);
    }

    @Transactional(readOnly = true)
    public MyBoxPageResponse getMyBoxList(Member member) {
        List<Box> myBoxList = boxService.getAllMyBoxListByOwner(member);
        List<MyBoxResponse> myBoxResponseList = myBoxList.stream()
                .map(MyBoxResponse::from)
                .toList();
        return MyBoxPageResponse.builder()
                .boxList(myBoxResponseList)
                .build();
    }

    @Transactional
    public BoxUpdateResponse updateMyBox(Member member, Long boxId, BoxUpdateRequest request) {
        Box box = boxService.getByBoxId(boxId);
        boxValidator.validateBoxOwner(box, member);
        box.update(request.getName(), request.getDescription(), request.getVisibleType());
        return BoxUpdateResponse.from(box);
    }

    @Transactional
    public void deleteMyBox(Member member, Long boxId) {
        Box box = boxService.getByBoxId(boxId);
        boxValidator.validateBoxOwner(box, member);
        boxContentCommandService.deleteAllByBox(box);
        boxService.deleteBox(box); // BoxMember 포함
        log.info("MyBox {} deleted for member: {} ", boxId, member.getNickname());
    }
}
