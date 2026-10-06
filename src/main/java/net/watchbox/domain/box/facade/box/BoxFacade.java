package net.watchbox.domain.box.facade.box;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.box.*;
import net.watchbox.domain.box.dto.box.request.BoxCreateRequest;
import net.watchbox.domain.box.dto.box.request.BoxUpdateRequest;
import net.watchbox.domain.box.dto.box.response.BoxCreateResponse;
import net.watchbox.domain.box.dto.box.response.BoxPageResponse;
import net.watchbox.domain.box.dto.box.response.BoxUpdateResponse;
import net.watchbox.domain.box.dto.history.BoxHistoryPageResponse;
import net.watchbox.domain.box.dto.history.BoxHistorySortOrder;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.facade.history.BoxHistoryFacade;
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
import net.watchbox.domain.box.entity.member.BoxMemberRole;
import net.watchbox.domain.box.service.history.BoxHistoryCommandService;
import net.watchbox.domain.box.entity.member.BoxMember;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoxFacade {
    private final BoxService boxService;
    private final BoxMemberService boxMemberService;
    private final BoxValidator boxValidator;
    private final BoxContentQueryService boxContentQueryService;
    private final BoxContentCommandService  boxContentCommandService;
    private final BoxHistoryFacade boxHistoryFacade;
    private final BoxHistoryCommandService boxHistoryCommandService;

    @Transactional(readOnly = true)
    public BoxItem getBox(Member member, Long boxId) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        if(box.getBoxType().equals(BoxType.MY)){
            boxValidator.validateBoxOwner(box, member);
        }else{
            boxValidator.validateBoxMember(box, member);
        }

        List<String> previewPosters = boxContentQueryService
                .getRecentPosterPathsByBoxes(List.of(box))
                .getOrDefault(box.getBoxId(), Collections.emptyList());
        return BoxItem.of(box, previewPosters, boxMemberService.getMyRoleOrElseNull(box, member));
    }

    @Transactional(readOnly = true)
    public BoxPageResponse getBoxPage(Member member) {
        List<Box> boxList = boxService.getAllBoxesByMember(member).stream()
        // lastContentAddedAt 기준 내림차순 정렬, null은 가장 맨 앞으로 (null 때문에 Java에서 정렬)
        .sorted(Comparator.comparing(Box::getLastContentAddedAt,
                Comparator.nullsFirst(Comparator.reverseOrder())))
        .toList();
        Map<Long, List<String>> posterMap = boxContentQueryService.getRecentPosterPathsByBoxes(boxList);
        Map<Long, BoxMemberRole> myRoleMap = boxMemberService.getMyRoleByBoxId(member); // 박스마다 권한을 조회하면 N+1 이라 한 번에 받아둔다.

        List<BoxItem> boxItemList = boxList.stream()
                .map(box -> BoxItem.of(box,
                        posterMap.getOrDefault(box.getBoxId(), Collections.emptyList()),
                        myRoleMap.get(box.getBoxId())))
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

    /**
     * 공유 박스에서 나간다. 소유자는 쓸 수 없다 — 박스가 주인 없이 남으므로 삭제가 맞다.
     *
     * <p>내가 담았던 콘텐츠도 함께 지운다. 남겨두면 삭제 권한이 담은 본인에게만 있어
     * <b>아무도 치울 수 없는 콘텐츠</b>로 남는다.
     */
    @Transactional
    public void leaveBox(Member member, Long boxId) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);

        if (box.getBoxType() == BoxType.MY) {
            throw new CustomException(ErrorCode.CANNOT_LEAVE_MY_BOX);
        }

        BoxMember boxMember = boxMemberService.getByBoxAndMemberOrElseThrow(box, member);
        if (boxMember.getRole() == BoxMemberRole.OWNER) {
            throw new CustomException(ErrorCode.CANNOT_LEAVE_OWNED_BOX);
        }

        boxContentCommandService.deleteAllByPublisherAndBox(member, box);
        // BoxMember 를 지우기 전에 남긴다. 히스토리는 Member 를 참조하므로 나간 뒤에도 조회된다.
        boxHistoryCommandService.memberLeft(box, member);
        boxMemberService.removeBoxMember(boxMember);

        log.info("Member {} left SharedBox {}", member.getNickname(), boxId);
    }

    @Transactional(readOnly = true)
    public BoxHistoryPageResponse getBoxHistoryPage(Member member, Long boxId, BoxHistorySortOrder sort, Long cursorId, int size) {
        return boxHistoryFacade.getBoxHistoryPage(member, boxId, sort, cursorId, size);
    }
}
