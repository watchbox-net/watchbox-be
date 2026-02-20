package net.watchbox.domain.box.facade.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.*;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.service.BoxMemberService;
import net.watchbox.domain.box.service.BoxValidator;
import net.watchbox.domain.box.service.BoxService;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SharedBoxFacade {
    private final BoxService boxService;
    private final BoxMemberService boxMemberService;
    private final BoxValidator boxValidator;
    private final BoxContentCommandService boxContentCommandService;

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
    public BoxUpdateResponse updateSharedBox(Member member, Long boxId, BoxUpdateRequest request) {
        Box box = boxService.findById(boxId);
        boxValidator.validateBoxEditor(box, member);
        box.update(request.getName(), request.getDescription(), request.getVisibleType());
        return BoxUpdateResponse.from(box);
    }

    @Transactional
    public void deleteSharedBox(Member member, Long boxId) {
        Box box = boxService.findById(boxId);
        boxValidator.validateBoxOwner(box, member);

        Map<Long, String> boxMembers = box.getBoxMembers().stream()
                .collect(Collectors.toMap(
                        bm -> bm.getMember().getMemberId(),
                        bm -> bm.getMember().getNickname()
                ));

        boxContentCommandService.deleteAllByBox(box);
        boxService.deleteBox(box); // BoxMember 포함

        log.info("SharedBox {} deleted by owner: {}, members: {}", boxId, member.getNickname(), boxMembers);
    }
}
