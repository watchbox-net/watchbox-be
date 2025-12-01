package net.watchpeople.domain.box.facade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchpeople.domain.box.dto.response.BoxShareRequestResponse;
import net.watchpeople.domain.box.service.SharedBoxRequestService;
import net.watchpeople.domain.member.entity.Member;
import net.watchpeople.domain.member.service.MemberService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SharedBoxFacade {
    private final MemberService memberService;
    private final SharedBoxRequestService sharedBoxRequestService;

    @Transactional
    public void requestSharedBox(Member sender, Long memberId) {
        Member receiver = memberService.findById(memberId);
        sharedBoxRequestService.requestSharedBox(sender, receiver);
    }

    public List<BoxShareRequestResponse> getSharedBoxRequests(Member member) {
        return sharedBoxRequestService.getSharedBoxRequests(member);
    }

    @Transactional
    public void acceptSharedBoxRequest(Long requestId) {
        sharedBoxRequestService.acceptSharedBoxRequest(requestId);
    }

    @Transactional
    public void rejectSharedBoxRequest(Long requestId) {
        sharedBoxRequestService.rejectSharedBoxRequest(requestId);
    }


}
