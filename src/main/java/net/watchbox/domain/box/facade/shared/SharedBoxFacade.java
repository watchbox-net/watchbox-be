package net.watchbox.domain.box.facade.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.BoxCreateRequest;
import net.watchbox.domain.box.dto.BoxCreateResponse;
import net.watchbox.domain.box.dto.SharedBoxListResponse;
import net.watchbox.domain.box.dto.SharedBoxResponse;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.service.BoxMemberService;
import net.watchbox.domain.box.service.BoxValidator;
import net.watchbox.domain.box.service.BoxService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SharedBoxFacade {
    private final BoxService boxService;
    private final BoxMemberService boxMemberService;
    private final BoxValidator boxValidator;

    @Transactional
    public BoxCreateResponse createSharedBox(Member member, BoxCreateRequest request) {
        Box box = boxService.createBox(request);
        boxMemberService.addOwnerToBox(member, box);
        return BoxCreateResponse.from(box, member.getMemberId());
    }

    @Transactional(readOnly = true)
    public SharedBoxResponse getSharedBox(Member member, Long boxId) {
        Box box = boxService.findById(boxId);
        boxValidator.validateBoxMember(box, member);
        return SharedBoxResponse.from(box);
    }

    @Transactional(readOnly = true)
    public SharedBoxListResponse getSharedBoxList(Member member) {
        List<Box> sharedBoxList = boxService.findAllSharedBoxListByMember(member);
        List<SharedBoxResponse> sharedBoxResponseList = sharedBoxList.stream()
                .map(SharedBoxResponse::from)
                .toList();
        return SharedBoxListResponse.builder()
                .sharedBoxList(sharedBoxResponseList)
                .build();
    }

    @Transactional
    public void deleteSharedBox(Member member, Long boxId) {
        Box box = boxService.findById(boxId);
        boxValidator.validateBoxOwner(box, member);
        boxService.deleteBox(box);
        log.info("SharedBox {} deleted for member: {} ", boxId, member.getNickname());
    }


}
