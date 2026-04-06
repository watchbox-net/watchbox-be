package net.watchbox.domain.box.controller.box;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.box.*;
import net.watchbox.domain.box.facade.box.SharedBoxFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/shared")
@Tag(name = "SharedBox", description = "공유 박스 생성/수정/삭제 API")
@Observed
public class SharedBoxController { // ToDo: Swagger로 확인하기
    private final SharedBoxFacade sharedBoxFacade;

    @Operation(summary = "공유 박스 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<BoxCreateResponse>> createSharedBox(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid BoxCreateRequest request
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(
                sharedBoxFacade.createSharedBox(member, request)
        ));
    }

    @Operation(summary = "공유 박스 단일 조회")
    @GetMapping("/{boxId}")
    public ResponseEntity<ApiResponse<SharedBoxResponse>> getSharedBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedBoxFacade.getSharedBox(member, boxId)
        ));
    }


    @Operation(summary = "내가 속한 공유 박스 리스트 조회", description = "박스 리스트, 개수 응답")
    @GetMapping
    public ResponseEntity<ApiResponse<SharedBoxPageResponse>> getMySharedBoxList(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedBoxFacade.getMySharedBoxList(member)
        ));
    }

    @Operation(summary = "공유 박스 수정", description = "이름, 설명, 공개 타입")
    @PatchMapping("/{boxId}")
    public ResponseEntity<ApiResponse<BoxUpdateResponse>> updateSharedBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId,
            @RequestBody @Valid BoxUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedBoxFacade.updateSharedBox(member, boxId, request)
        ));
    }

    @Operation(summary = "공유 박스 삭제", description = "BoxMember, BoxContent 모두 삭제 <br>" +
            "로그 남김")
    @DeleteMapping("/{boxId}")
    public ResponseEntity<ApiResponse<Void>> deleteSharedBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId
    ) {
        sharedBoxFacade.deleteSharedBox(member, boxId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
