package net.watchbox.domain.box.controller.invitation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.Invitation.InvitationReceivedResponse;
import net.watchbox.domain.box.dto.Invitation.InvitationSentResponse;
import net.watchbox.domain.box.facade.invitation.BoxInvitationFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/shared/invitations")
@Tag(name = "4-4 [Box] Invitation")
public class BoxInvitationController {
    private final BoxInvitationFacade boxInvitationFacade;

    @Operation(summary = "받은 초대 존재 유무",
            description = "받은 PENDING 초대가 있는지 boolean 으로만 반환. 화면 진입 전 배지/점 표시용 (목록 조회보다 가벼움).")
    @GetMapping("/received/exists")
    public ResponseEntity<ApiResponse<Boolean>> hasReceivedInvitation(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(boxInvitationFacade.hasReceivedInvitation(member))
        );
    }

    @Operation(summary = "받은 초대 리스트 조회") // 일단 모든 요청상태 조회
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<InvitationReceivedResponse>>> getBoxInvitationsReceived(
            @AuthenticationPrincipal Member member
    ){
        return ResponseEntity.ok(
                ApiResponse.success(boxInvitationFacade.getBoxInvitationsReceived(member))
        );
    }

    @Operation(summary = "보낸 초대 리스트 조회") // 일단 모든 요청상태 조회
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<InvitationSentResponse>>> getBoxInvitationsSent(
            @AuthenticationPrincipal Member member
//            @RequestParam (required = true) RequestStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(boxInvitationFacade.getBoxInvitationsSent(member))
        );
    }

    @Operation(summary = "박스 초대 보내기")
    @PostMapping("/{boxId}/{memberId}")
    public ResponseEntity<ApiResponse<InvitationSentResponse>> inviteToBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId,
            @PathVariable Long memberId
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(
                boxInvitationFacade.inviteToBox(member, boxId, memberId)
        ));
    }

    @Operation(summary = "받은 초대 수락하기")
    @PatchMapping("/{requestId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptBoxInvitation(
            @AuthenticationPrincipal Member member,
            @PathVariable Long requestId
    ) {
        boxInvitationFacade.acceptBoxInvitation(member, requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "받은 초대 거절하기")
    @PatchMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectBoxInvitation(
            @AuthenticationPrincipal Member member,
            @PathVariable Long requestId
    ) {
        boxInvitationFacade.rejectBoxInvitation(member, requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "보낸 초대 요청 취소")
    @DeleteMapping("{requestId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBoxInvitation(
            @AuthenticationPrincipal Member member,
            @PathVariable Long requestId
    ){
        boxInvitationFacade.cancelBoxInvitation(member, requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "거절 당한 초대 요청 삭제")
    @DeleteMapping("{requestId}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteBoxInvitation(
            @AuthenticationPrincipal Member member,
            @PathVariable Long requestId
    ){
        boxInvitationFacade.deleteBoxInvitation(member, requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
