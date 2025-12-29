package net.watchbox.domain.box.facade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.response.InviteBoxRequestResponse;
import net.watchbox.domain.box.entity.SharedBox;
import net.watchbox.domain.box.entity.member.BoxMember;
import net.watchbox.domain.box.entity.request.InviteBoxRequest;
import net.watchbox.domain.box.service.BoxMemberService;
import net.watchbox.domain.box.service.SharedBoxService;
import net.watchbox.domain.box.service.request.InviteBoxRequestService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InviteBoxRequestFacade {
    private final MemberService memberService;
    private final InviteBoxRequestService inviteBoxRequestService;
    private final BoxMemberService boxMemberService;
    private final SharedBoxService sharedBoxService;

    public void requestSharedBox(Member inviter, Long boxId, Long inviteeId) {
        Member receiver = memberService.findById(inviteeId);
        SharedBox sharedBox = sharedBoxService.findById(boxId);
        inviteBoxRequestService.requestSharedBox(inviter, sharedBox, receiver);
    }

    public List<InviteBoxRequestResponse> getInviteBoxRequestsSent(Member member) {
        return inviteBoxRequestService.getInviteBoxRequestsSent(member);
    }

    @Transactional
    public void acceptBoxInviteRequest(Member member, Long requestId) {
        InviteBoxRequest inviteBoxRequest = inviteBoxRequestService.findById(requestId);
        SharedBox sharedBox = inviteBoxRequest.getSharedBox();
        inviteBoxRequestService.acceptBoxInviteRequest(member, inviteBoxRequest);
        boxMemberService.addEditorToBox(member, sharedBox);
        log.info("BoxMember added for member: " + member.getNickname());

        List<BoxMember> boxMembers = boxMemberService.findAllBySharedBox(sharedBox);
        List<String> memberNames = boxMembers.stream()
                .map(boxMember -> boxMember.getMember().getNickname())
                .toList();
        sharedBox.updateAutoTitle(memberNames);
        log.info("SharedBox {} updated title to: {}", sharedBox.getBoxId(), sharedBox.getTitle());
    }

    public void rejectBoxInviteRequest(Member member, Long requestId) {
        InviteBoxRequest inviteBoxRequest = inviteBoxRequestService.findById(requestId);
        inviteBoxRequestService.rejectBoxInviteRequest(member, inviteBoxRequest);
    }

}
