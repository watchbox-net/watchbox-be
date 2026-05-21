package net.watchbox.domain.box.controller.box;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.box.*;
import net.watchbox.domain.box.dto.box.request.BoxCreateRequest;
import net.watchbox.domain.box.dto.box.request.BoxUpdateRequest;
import net.watchbox.domain.box.dto.box.response.BoxCreateResponse;
import net.watchbox.domain.box.dto.box.response.BoxPageResponse;
import net.watchbox.domain.box.dto.box.response.BoxUpdateResponse;
import net.watchbox.domain.box.facade.box.BoxFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes")
@Tag(name = "Box", description = "박스 통합 API")
@Observed
public class BoxController {
    private final BoxFacade boxFacade;

    @Operation(summary = "박스 단일 조회", description = "박스 생성/수정 후 응답 <br>"
            + "응답에 포스터, 박스멤버 X")
    @GetMapping("/{boxId}")
    public ResponseEntity<ApiResponse<BoxItem>> getBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                boxFacade.getBox(member, boxId)
        ));
    }

    @Operation(summary = "박스 페이지 조회", description = "박스 페이지 응답 <br>"
            + "모든 마이 박스 + 내가 속한 모든 공유 박스 조회 <br>"
            + "응답에 포스터, 박스멤버 O <br>"
            + "정렬은 일단 최신 업데이트순")
    @GetMapping
    public ResponseEntity<ApiResponse<BoxPageResponse>> getBoxPage(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                boxFacade.getBoxPage(member)
        ));
    }


    @Operation(summary = "박스 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<BoxCreateResponse>> createBox(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid BoxCreateRequest request
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(
                boxFacade.createBox(member, request)
        ));
    }

    @Operation(summary = "박스 수정", description = "이름, 설명, 공개 타입")
    @PatchMapping("/{boxId}")
    public ResponseEntity<ApiResponse<BoxUpdateResponse>> updateBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId,
            @RequestBody @Valid BoxUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                boxFacade.updateBox(member, boxId, request)
        ));
    }

    @Operation(summary = "박스 삭제",
            description = "마이 박스 - BoxMember 본인과 BoxContent 모두 삭제 <br> "
                    + "공유 박스 - BoxMember 전부와 BoxContent 모두 삭제 <br>"
                    + "로그 남김")
    @DeleteMapping("/{boxId}")
    public ResponseEntity<ApiResponse<Void>> deleteBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId
    ) {
        boxFacade.deleteBox(member, boxId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
