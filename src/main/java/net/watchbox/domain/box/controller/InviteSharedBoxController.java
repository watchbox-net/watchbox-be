package net.watchbox.domain.box.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.response.Invitation.InvitationReceivedResponse;
import net.watchbox.domain.box.dto.response.Invitation.InvitationSentResponse;
import net.watchbox.domain.box.facade.InviteSharedBoxFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/shared/invitations")
@Tag(name = "InviteSharedBox", description = "InviteSharedBox API")
public class InviteSharedBoxController {
    private final InviteSharedBoxFacade inviteSharedBoxFacade;

    /**
     * 공유 박스에 초대하기
     * 공유 박스 초대 알림 전송하기(SSE) to 초대 받는사람
     * [보류] 공유 박스 거절 알림 전송하기(SSE) to 초대 보낸사람
     *
     * 공유 박스 받은초대 수락
     * 공유 박스 받은초대 거절
     *
     * 공유 박스 초대 요청 리스트 조회 (두 사이드 sent, received)
     * └── 보낸초대 리스트
     * └── 받은초대 리스트
     *
     */

    // 공유 박스에 초대하기
    @PostMapping("/{boxId}/{inviteeId}")
    public ResponseEntity<ApiResponse<Void>> inviteToBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId,
            @PathVariable Long inviteeId
    ) {
        inviteSharedBoxFacade.inviteToBox(member, boxId, inviteeId);
        return ResponseEntity.status(201).body(ApiResponse.success());
    }

    // 공유 박스 받은초대 수락하기
    @PatchMapping("/accept/{requestId}")
    public ResponseEntity<ApiResponse<Void>> acceptBoxInvitation(
            @AuthenticationPrincipal Member member,
            @PathVariable Long requestId
    ) {
        inviteSharedBoxFacade.acceptBoxInvitation(member, requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 공유 박스 받은초대 요청 거절하기
    @PatchMapping("/reject/{requestId}")
    public ResponseEntity<ApiResponse<Void>> rejectBoxInvitation(
            @AuthenticationPrincipal Member member,
            @PathVariable Long requestId
    ) {
        inviteSharedBoxFacade.rejectBoxInvitation(member, requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 공유 박스 보낸초대 리스트 조회
    // 일단 모든 요청상태 조회
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<InvitationSentResponse>>> getBoxInvitationsSent(
            @AuthenticationPrincipal Member member
//            @RequestParam (required = true) RequestStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(inviteSharedBoxFacade.getBoxInvitationsSent(member))
        );
    }

    // 공유 박스 받은초대 리스트 조회
    // 일단 모든 요청상태 조회
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<InvitationReceivedResponse>>> getBoxInvitationsReceived(
            @AuthenticationPrincipal Member member
    ){
        return ResponseEntity.ok(
                ApiResponse.success(inviteSharedBoxFacade.getBoxInvitationsReceived(member))
        );
    }
}
