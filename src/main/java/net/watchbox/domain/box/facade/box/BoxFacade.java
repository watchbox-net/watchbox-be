package net.watchbox.domain.box.facade.box;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.box.BoxPageResponse;
import net.watchbox.domain.box.dto.box.BoxResponse;
import net.watchbox.domain.box.dto.member.BoxMemberResponse;
import net.watchbox.domain.box.entity.box.Box;
import net.watchbox.domain.box.entity.box.BoxType;
import net.watchbox.domain.box.service.box.BoxService;
import net.watchbox.domain.box.service.content.BoxContentQueryService;
import net.watchbox.domain.box.service.member.BoxMemberService;
import net.watchbox.domain.box.service.validation.BoxValidator;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoxFacade {
    private final BoxService boxService;
    private final BoxMemberService boxMemberService;
    private final BoxValidator boxValidator;
    private final BoxContentQueryService boxContentQueryService;

    @Transactional(readOnly = true)
    public BoxResponse getBox(Member member, Long boxId) {
        Box box = boxService.getByBoxIdOrElseThrow(boxId);
        if(box.getBoxType().equals(BoxType.MY)){
            boxValidator.validateBoxOwner(box, member);
        }else{
            boxValidator.validateBoxMember(box, member);
        }
        return BoxResponse.from(box);
    }

    @Transactional(readOnly = true)
    public BoxPageResponse getBoxPage(Member member) {
        List<Box> myBoxList = boxService.getAllMyBoxListByOwner(member);
        List<Box> sharedBoxList = boxService.getAllSharedBoxListByMember(member);

        List<Box> boxList = Stream.concat(
                myBoxList.stream(),
                sharedBoxList.stream()
        )
        // lastContentAddedAt 기준 내림차순 정렬, null은 가장 맨 앞으로
        .sorted(Comparator.comparing(Box::getLastContentAddedAt,
                Comparator.nullsFirst(Comparator.reverseOrder())))
        .toList();
        Map<Long, List<String>> posterMap = boxContentQueryService.getRecentPosterPathsByBoxes(boxList);

        List<BoxResponse> boxResponseList = boxList.stream()
                .map(box -> {
                    if (box.getBoxType().equals(BoxType.MY)) {
                        return BoxResponse.of(box, posterMap.getOrDefault(box.getBoxId(), Collections.emptyList()));
                    } else {
                        List<BoxMemberResponse> memberList = box.getBoxMembers().stream()
                                .map(BoxMemberResponse::from)
                                .collect(Collectors.toList());
                        return BoxResponse.of(box, posterMap.getOrDefault(box.getBoxId(), Collections.emptyList()), memberList);
                    }
                })
                .toList();

        return BoxPageResponse.builder()
                .boxList(boxResponseList)
                .boxCount(boxResponseList.size())
                .build();
    }
}
