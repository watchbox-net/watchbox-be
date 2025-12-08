package net.watchpeople.domain.box.facade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.watchpeople.domain.box.dto.response.CreateBoxRequestResponse;
import net.watchpeople.domain.box.service.CreateBoxRequestService;
import net.watchpeople.domain.member.entity.Member;
import net.watchpeople.domain.member.service.MemberService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SharedBoxFacade {
    private final MemberService memberService;
    private final CreateBoxRequestService createBoxRequestService;

    @Transactional
    public void requestSharedBox(Member sender, Long memberId) {
        Member receiver = memberService.findById(memberId);
        createBoxRequestService.requestSharedBox(sender, receiver);
    }

    public List<CreateBoxRequestResponse> getSharedBoxRequests(Member member) {
        return createBoxRequestService.getSharedBoxRequests(member);
    }

    @Transactional
    public void acceptSharedBoxRequest(Long requestId) {
        createBoxRequestService.acceptSharedBoxRequest(requestId);
    }

    @Transactional
    public void rejectSharedBoxRequest(Long requestId) {
        createBoxRequestService.rejectSharedBoxRequest(requestId);
    }


}
