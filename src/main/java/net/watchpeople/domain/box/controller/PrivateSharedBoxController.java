package net.watchpeople.domain.box.controller;

import lombok.RequiredArgsConstructor;
import net.watchpeople.domain.box.dto.response.BoxShareRequestResponse;
import net.watchpeople.domain.box.facade.SharedBoxFacade;
import net.watchpeople.domain.member.entity.Member;
import net.watchpeople.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/shared")
public class PrivateSharedBoxController {
    private final SharedBoxFacade sharedBoxFacade;

    // 공유 박스 신청하기
    @PostMapping("/request/{receiverId}")
    public ResponseEntity<ApiResponse<Void>> requestSharedBox(
            @AuthenticationPrincipal Member member,
            Long receiverId
    ) {
        sharedBoxFacade.requestSharedBox(member, receiverId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 공유 박스 신청 리스트 조회
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<BoxShareRequestResponse>>> getBoxShareRequests(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(sharedBoxFacade.getSharedBoxRequests(member)));
    }

    // 공유 박스 신청 수락하기
    @PatchMapping("/accept/{requestId}")
    public ResponseEntity<ApiResponse<Void>> acceptSharedBoxRequest(
            Long requestId
    ) {
        sharedBoxFacade.acceptSharedBoxRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 공유 박스 신청 거절하기
    @PatchMapping("/reject/{requestId}")
    public ResponseEntity<ApiResponse<Void>> rejectSharedBoxRequest(
            Long requestId
    ) {
        sharedBoxFacade.rejectSharedBoxRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
