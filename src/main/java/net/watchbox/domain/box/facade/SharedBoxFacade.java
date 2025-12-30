package net.watchbox.domain.box.facade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.response.SharedBoxResponse;
import net.watchbox.domain.box.entity.SharedBox;
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
        SharedBox sharedBox = sharedBoxService.createSharedBox(member);
        boxMemberService.addOwnerToBox(member, sharedBox);
        return new SharedBoxResponse(sharedBox.getBoxId(), sharedBox.getTitle());
    }

    public List<SharedBoxResponse> getSharedBoxes(Member member) {
        return boxMemberService.findAllByMember(member).stream()
                .map(boxMember -> {
                    SharedBox box = boxMember.getSharedBox();
                    return new SharedBoxResponse(box.getBoxId(), box.getTitle());
                })
                .toList();
    }

    public void deleteSharedBox(Member member, Long boxId) {
        SharedBox sharedBox = sharedBoxService.findById(boxId);
        boxMemberService.validateSharedBoxOwner(sharedBox, member);
        sharedBoxService.deleteSharedBox(sharedBox);
        log.info("SharedBox {} deleted for member: {} ", boxId, member.getNickname());
    }
}
