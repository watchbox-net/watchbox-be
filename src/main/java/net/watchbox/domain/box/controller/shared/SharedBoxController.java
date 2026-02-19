package net.watchbox.domain.box.controller.shared;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.BoxCreateRequest;
import net.watchbox.domain.box.dto.BoxCreateResponse;
import net.watchbox.domain.box.dto.SharedBoxListResponse;
import net.watchbox.domain.box.dto.SharedBoxResponse;
import net.watchbox.domain.box.facade.shared.SharedBoxFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/shared")
@Tag(name = "SharedBox CRUD")
// ToDo: Swagger로 확인하기
public class SharedBoxController {
    private final SharedBoxFacade sharedBoxFacade;

    @PostMapping
    @Operation(summary = "공유 박스 생성")
    public ResponseEntity<ApiResponse<BoxCreateResponse>> createSharedBox(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid BoxCreateRequest request
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(
                sharedBoxFacade.createSharedBox(member, request)
        ));
    }

    // ToDo: 공유 박스 단일 조회하기
    @GetMapping("/{boxId}")
    @Operation(summary = "공유 박스 단일 조회")
    public ResponseEntity<ApiResponse<SharedBoxResponse>> getSharedBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedBoxFacade.getSharedBox(member, boxId)
        ));
    }


    // ToDo: 공유 박스 리스트 조회하기
    @GetMapping
    public ResponseEntity<ApiResponse<SharedBoxListResponse>> getSharedBoxList(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedBoxFacade.getSharedBoxList(member)
        ));
    }

    // ToDo: 공유 박스 수정하기
    // ToDo: 공유 박스 삭제하기
    @DeleteMapping("/{boxId}")
    public ResponseEntity<ApiResponse<Void>> deleteSharedBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId
    ) {
        sharedBoxFacade.deleteSharedBox(member, boxId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
