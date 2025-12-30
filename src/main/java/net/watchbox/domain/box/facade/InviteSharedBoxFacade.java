package net.watchbox.domain.box.facade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.box.dto.response.Invitation.InvitationReceivedResponse;
import net.watchbox.domain.box.dto.response.Invitation.InvitationSentResponse;
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
public class InviteSharedBoxFacade {
    private final MemberService memberService;
    private final InviteBoxRequestService inviteBoxRequestService;
    private final BoxMemberService boxMemberService;
    private final SharedBoxService sharedBoxService;

    public void inviteToBox(Member sender, Long boxId, Long receiverId) {
        Member receiver = memberService.findById(receiverId);
        SharedBox sharedBox = sharedBoxService.findById(boxId);

        // 기존 박스 멤버인지 검증
        boxMemberService.validateExistingBoxMember(sharedBox, receiver);
        // 초대 요청 중복 검증
        inviteBoxRequestService.validateDuplicateInviteRequest(sharedBox, receiver);
        // 초대 요청 생성
        inviteBoxRequestService.inviteToBox(sender, sharedBox, receiver);
    }

    public List<InvitationSentResponse> getBoxInvitationsSent(Member member) {
        return inviteBoxRequestService.getBoxInvitationsSent(member);
    }

    public List<InvitationReceivedResponse> getBoxInvitationsReceived(Member member) {
        return inviteBoxRequestService.getBoxInvitationsReceived(member);
    }

    @Transactional
    public void acceptBoxInvitation(Member member, Long requestId) {
        InviteBoxRequest inviteBoxRequest = inviteBoxRequestService.findById(requestId);
        SharedBox sharedBox = inviteBoxRequest.getSharedBox();

        // 이미 처리된 요청인지지 검증
        inviteBoxRequestService.validatePendingInviteRequest(inviteBoxRequest);
        // 수락 처리
        inviteBoxRequestService.acceptBoxInvitation(member, inviteBoxRequest);
        // 박스 멤버(EDITOR 권한)로 추가
        boxMemberService.addEditorToBox(member, sharedBox);

        List<BoxMember> boxMembers = boxMemberService.findAllBySharedBox(sharedBox);
        List<String> memberNames = boxMembers.stream()
                .map(boxMember -> boxMember.getMember().getNickname())
                .toList();
        sharedBox.updateAutoTitle(memberNames);
    }

    public void rejectBoxInvitation(Member member, Long requestId) {
        InviteBoxRequest inviteBoxRequest = inviteBoxRequestService.findById(requestId);
        // 이미 처리된 요청인지지 검증
        inviteBoxRequestService.validatePendingInviteRequest(inviteBoxRequest);
        // 거절 처리
        inviteBoxRequestService.rejectBoxInvitation(member, inviteBoxRequest);
    }

}
