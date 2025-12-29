package net.watchbox.domain.box.controller;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.response.InviteBoxRequestResponse;
import net.watchbox.domain.box.facade.InviteBoxRequestFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/shared")
public class InviteBoxRequestController {
    private final InviteBoxRequestFacade inviteBoxRequestFacade;

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
    @PostMapping("/{boxId}/invite/{inviteeId}")
    public ResponseEntity<ApiResponse<Void>> requestSharedBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId,
            @PathVariable Long inviteeId
    ) {
        inviteBoxRequestFacade.requestSharedBox(member, boxId, inviteeId);
        return ResponseEntity.status(201).body(ApiResponse.success());
    }

    // 공유 박스 받은초대 수락하기
    @PatchMapping("/accept/{requestId}")
    public ResponseEntity<ApiResponse<Void>> acceptSharedBoxRequest(
            @AuthenticationPrincipal Member member,
            @PathVariable Long requestId
    ) {
        inviteBoxRequestFacade.acceptBoxInviteRequest(member, requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 공유 박스 받은초대 요청 거절하기
    @PatchMapping("/reject/{requestId}")
    public ResponseEntity<ApiResponse<Void>> rejectSharedBoxRequest(
            @AuthenticationPrincipal Member member,
            @PathVariable Long requestId
    ) {
        inviteBoxRequestFacade.rejectBoxInviteRequest(member, requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 공유 박스 보낸초대 리스트 조회
    @GetMapping("/invitations/sent")
    public ResponseEntity<ApiResponse<List<InviteBoxRequestResponse>>> getInviteBoxRequestsSent(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(inviteBoxRequestFacade.getInviteBoxRequestsSent(member))
        );
    }

    // 공유 박스 받은초대 리스트 조회
//    @GetMapping("/invitations/received")
//    public ResponseEntity<ApiResponse<List<InviteBoxRequestResponse>>> getInviteBoxRequestsReceived(
//            @AuthenticationPrincipal Member member
//    )
}
