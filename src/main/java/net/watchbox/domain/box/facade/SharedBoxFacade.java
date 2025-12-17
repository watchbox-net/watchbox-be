package net.watchbox.domain.box.facade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.response.InviteBoxRequestResponse;
import net.watchbox.domain.box.service.shared.InviteBoxRequestService;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.service.MemberService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SharedBoxFacade {
    private final MemberService memberService;
    private final InviteBoxRequestService inviteBoxRequestService;

    @Transactional
    public void requestSharedBox(Member inviter, Long inviteeId) {
        Member receiver = memberService.findById(inviteeId);
        inviteBoxRequestService.requestSharedBox(inviter, receiver);
    }

    public List<InviteBoxRequestResponse> getSharedBoxRequests(Member member) {
        return inviteBoxRequestService.getSharedBoxRequests(member);
    }

    @Transactional
    public void acceptSharedBoxRequest(Member member, Long requestId) {
        inviteBoxRequestService.acceptSharedBoxRequest(member, requestId);
    }

    @Transactional
    public void rejectSharedBoxRequest(Member member, Long requestId) {
        inviteBoxRequestService.rejectSharedBoxRequest(member, requestId);
    }


}
