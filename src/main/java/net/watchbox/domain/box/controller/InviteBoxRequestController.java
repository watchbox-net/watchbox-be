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

    // 공유 박스 생성하기


    // 공유 박스에 초대하기
    @PostMapping("/invite/{inviteeId}")
    public ResponseEntity<ApiResponse<Void>> requestSharedBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long inviteeId
    ) {
        inviteBoxRequestFacade.requestSharedBox(member, inviteeId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 공유 박스 생성 신청 리스트 조회
    @GetMapping("/invite")
    public ResponseEntity<ApiResponse<List<InviteBoxRequestResponse>>> getBoxShareRequests(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(inviteBoxRequestFacade.getSharedBoxRequests(member)));
    }

    // 공유 박스 생성 신청 수락하기
    @PatchMapping("/accept/{requestId}")
    public ResponseEntity<ApiResponse<Void>> acceptSharedBoxRequest(
            @AuthenticationPrincipal Member member,
            Long requestId
    ) {
        inviteBoxRequestFacade.acceptSharedBoxRequest(member, requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 공유 박스 생성 신청 거절하기
    @PatchMapping("/reject/{requestId}")
    public ResponseEntity<ApiResponse<Void>> rejectSharedBoxRequest(
            @AuthenticationPrincipal Member member,
            Long requestId
    ) {
        inviteBoxRequestFacade.rejectSharedBoxRequest(member, requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }


}
