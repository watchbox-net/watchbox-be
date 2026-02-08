package net.watchbox.domain.box.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.response.SharedBoxResponse;
import net.watchbox.domain.box.facade.SharedBoxFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/shared")
@Tag(name = "SharedBox", description = "SharedBox API")
public class BoxController {
    private final SharedBoxFacade sharedBoxFacade;
    /**
     * 공유 박스 생성하기
     * 공유 박스 리스트 조회하기
     * 공유 박스 삭제하기
     *
     * [보류]
     * 공유 박스 제목 수정하기
     * 공유 박스에서 나가기
     * 공유 박스에서 멤버 추방하기
     *
     * [public box][보류]
     * 공유 박스 공개로 전환하기, 비공개로 전환하기
     * 공유 박스에 참가 요청하기
     * 공유 박스 참가 요청 승인하기
     * 공유 박스 참가 요청 거절하기
     * 공유 박스 참가 요청 리스트 조회하기
     */

    // 공유 박스 생성하기
    @PostMapping
    public ResponseEntity<ApiResponse<SharedBoxResponse>> createSharedBox(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedBoxFacade.createSharedBox(member)
        ));
    }

    // 공유 박스 리스트 조회하기
    @GetMapping
    public ResponseEntity<ApiResponse<List<SharedBoxResponse>>> getSharedBoxes(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedBoxFacade.getSharedBoxes(member)
        ));
    }

    // 공유 박스 삭제하기
    @DeleteMapping("/{boxId}")
    public ResponseEntity<ApiResponse<Void>> deleteSharedBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId
    ) {
        sharedBoxFacade.deleteSharedBox(member, boxId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
