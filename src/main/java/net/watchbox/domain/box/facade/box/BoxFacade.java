package net.watchbox.domain.box.facade.box;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.box.*;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.content.BoxContentCommandService;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.box.service.member.BoxMemberService;
import net.watchbox.domain.box.service.validation.BoxValidator;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoxFacade {
    private final BoxService boxService;
    private final BoxMemberService boxMemberService;
    private final BoxValidator boxValidator;
    private final BoxContentQueryService boxContentQueryService;
    private final BoxContentCommandService  boxContentCommandService;

    @Transactional(readOnly = true)
    public BoxItem getBox(Member member, Long boxId) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        if(box.getBoxType().equals(BoxType.MY)){
            boxValidator.validateBoxOwner(box, member);
        }else{
            boxValidator.validateBoxMember(box, member);
        }
        return BoxItem.from(box);
    }

    @Transactional(readOnly = true)
    public BoxPageResponse getBoxPage(Member member) {
        List<Box> boxList = boxService.getAllBoxesByMember(member).stream()
        // lastContentAddedAt 기준 내림차순 정렬, null은 가장 맨 앞으로 (null 때문에 Java에서 정렬)
        .sorted(Comparator.comparing(Box::getLastContentAddedAt,
                Comparator.nullsFirst(Comparator.reverseOrder())))
        .toList();
        Map<Long, List<String>> posterMap = boxContentQueryService.getRecentPosterPathsByBoxes(boxList);

        List<BoxItem> boxItemList = boxList.stream()
                .map(box -> BoxItem.of(box,
                        posterMap.getOrDefault(box.getBoxId(), Collections.emptyList())))
                .toList();

        return BoxPageResponse.builder()
                .boxItemList(boxItemList)
                .totalCount((long) boxItemList.size())
                .build();
    }

    @Transactional
    public BoxCreateResponse createBox(Member member, BoxCreateRequest request) {
        Box box = boxService.createBox(member, request);
        boxMemberService.addOwnerToBox(member, box);
        return BoxCreateResponse.from(box);
    }

    @Transactional
    public BoxUpdateResponse updateBox(Member member, Long boxId, @Valid BoxUpdateRequest request) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        if (box.getBoxType().equals(BoxType.MY)) {
            boxValidator.validateBoxOwner(box, member);
        } else {
            boxValidator.validateBoxEditor(box, member);
        }
        box.update(request.getName(), request.getDescription(), request.getVisibleType());
        return BoxUpdateResponse.from(box);
    }

    @Transactional
    public void deleteBox(Member member, Long boxId) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        boxValidator.validateBoxOwner(box, member);

        // 박스는 최소 1개 유지 (마지막 마이 박스 삭제 금지)
        if (boxService.countMyBoxByOwner(member) <= 1) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_LAST_MY_BOX);
        }

        boxContentCommandService.deleteAllByBox(box);
        boxService.deleteBox(box); // BoxMember 포함

        if (box.getBoxType().equals(BoxType.MY)) {
            log.info("Box {} deleted for member: {} ", boxId, member.getNickname());
        } else {
            Map<Long, String> boxMembers = box.getBoxMembers().stream()
                    .collect(Collectors.toMap(
                            bm -> bm.getMember().getMemberId(),
                            bm -> bm.getMember().getNickname()
                    ));
            log.info("SharedBox {} deleted by owner: {}, members: {}", boxId, member.getNickname(), boxMembers);
        }
    }
}
