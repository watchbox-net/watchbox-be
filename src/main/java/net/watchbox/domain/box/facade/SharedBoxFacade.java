package net.watchbox.domain.box.facade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.response.SharedBoxResponse;
import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.service.BoxMemberService;
import net.watchbox.domain.box.service.SharedBoxService;
import net.watchbox.domain.member.entity.Member;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SharedBoxFacade {
    private final SharedBoxService sharedBoxService;
    private final BoxMemberService boxMemberService;

    @Transactional
    public SharedBoxResponse createSharedBox(Member member) {
        Box box = sharedBoxService.createSharedBox(member);
        boxMemberService.addOwnerToBox(member, box);
        return new SharedBoxResponse(box.getBoxId(), box.getTitle());
    }

    public List<SharedBoxResponse> getSharedBoxes(Member member) {
        return boxMemberService.findAllByMember(member).stream()
                .map(boxMember -> {
                    Box box = boxMember.getBox();
                    return new SharedBoxResponse(box.getBoxId(), box.getTitle());
                })
                .toList();
    }

    public void deleteSharedBox(Member member, Long boxId) {
        Box box = sharedBoxService.findById(boxId);
        boxMemberService.validateSharedBoxOwner(box, member);
        sharedBoxService.deleteSharedBox(box);
        log.info("SharedBox {} deleted for member: {} ", boxId, member.getNickname());
    }
}
