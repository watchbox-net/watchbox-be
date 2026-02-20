package net.watchbox.domain.box.controller.my;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.*;
import net.watchbox.domain.box.facade.my.MyBoxFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/my")
@Tag(name = "MyBox CRUD")
public class MyBoxController { // ToDo: Swagger로 확인하기
    private final MyBoxFacade myBoxFacade;

    @PostMapping
    @Operation(summary = "마이 박스 생성")
    public ResponseEntity<ApiResponse<BoxCreateResponse>> createMyBox(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid BoxCreateRequest request
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(
                myBoxFacade.createMyBox(member, request)
        ));
    }

    @GetMapping("/{boxId}")
    @Operation(summary = "마이 박스 단일 조회")
    public ResponseEntity<ApiResponse<MyBoxResponse>> getMyBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                myBoxFacade.getMyBox(member, boxId)
        ));
    }

    @GetMapping
    @Operation(summary = "마이 박스 리스트 조회", description = "컨텐츠 리스트, 개수 응답")
    public ResponseEntity<ApiResponse<MyBoxListResponse>> getMyBoxList(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                myBoxFacade.getMyBoxList(member)
        ));
    }

    @PatchMapping("/{boxId}")
    @Operation(summary = "마이 박스 수정", description = "이름, 설명")
    public ResponseEntity<ApiResponse<BoxUpdateResponse>> updateMyBox(
        @AuthenticationPrincipal Member member,
        @PathVariable Long boxId,
        @RequestBody @Valid BoxUpdateRequest request
    ) {
            return ResponseEntity.ok(ApiResponse.success(
                    myBoxFacade.updateMyBox(member, boxId, request)
            ));
    }

    // ToDo: 마이 박스 -> 공유 박스 전환하기

    @DeleteMapping("/{boxId}")
    @Operation(summary = "마이 박스 삭제", description = "BoxMember 본인과 BoxContent 모두 삭제 " +
            "\n로그 남김")
    public ResponseEntity<ApiResponse<Void>> deleteMyBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId
    ) {
        myBoxFacade.deleteMyBox(member, boxId);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
