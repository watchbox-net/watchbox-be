package net.watchbox.domain.box.facade.box;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.box.*;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.service.member.BoxMemberService;
import net.watchbox.domain.box.service.validation.BoxValidator;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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
    private final BoxContentQueryService boxContentQueryService;

    @Transactional
    public BoxCreateResponse createSharedBox(Member member, BoxCreateRequest request) {
        Box box = boxService.createBox(member, request, BoxType.SHARED);
        boxMemberService.addOwnerToBox(member, box);
        return BoxCreateResponse.from(box, member.getMemberId());
    }

    @Transactional(readOnly = true)
    public SharedBoxResponse getSharedBox(Member member, Long boxId) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        boxValidator.validateBoxMember(box, member);
        return SharedBoxResponse.of(box, Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public SharedBoxPageResponse getMySharedBoxList(Member member) {
        List<Box> sharedBoxList = boxService.getAllSharedBoxListByMember(member);
        Map<Long, List<String>> posterMap = boxContentQueryService.getRecentPosterPathsByBoxes(sharedBoxList);

        List<SharedBoxResponse> sharedBoxResponseList = sharedBoxList.stream()
                .map(box -> SharedBoxResponse.of(box,
                        posterMap.getOrDefault(box.getBoxId(), Collections.emptyList())))
                .toList();
        return SharedBoxPageResponse.builder()
                .sharedBoxList(sharedBoxResponseList)
                .boxCount(sharedBoxResponseList.size())
                .build();
    }

    @Transactional
    public BoxUpdateResponse updateSharedBox(Member member, Long boxId, BoxUpdateRequest request) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        boxValidator.validateBoxEditor(box, member);
        box.update(request.getName(), request.getDescription(), request.getVisibleType());
        return BoxUpdateResponse.from(box);
    }

    @Transactional
    public void deleteSharedBox(Member member, Long boxId) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
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
