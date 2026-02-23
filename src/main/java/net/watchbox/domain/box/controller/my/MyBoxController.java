package net.watchbox.domain.box.controller.my;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.box.*;
import net.watchbox.domain.box.facade.my.MyBoxFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/my")
@Tag(name = "MyBox", description = "마이 박스 CRUD API")
public class MyBoxController { // ToDo: Swagger로 확인하기
    private final MyBoxFacade myBoxFacade;

    @Operation(summary = "마이 박스 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<BoxCreateResponse>> createMyBox(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid BoxCreateRequest request
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(
                myBoxFacade.createMyBox(member, request)
        ));
    }

    @Operation(summary = "마이 박스 단일 조회")
    @GetMapping("/{boxId}")
    public ResponseEntity<ApiResponse<MyBoxResponse>> getMyBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                myBoxFacade.getMyBox(member, boxId)
        ));
    }

    @Operation(summary = "마이 박스 리스트 조회", description = "박스 리스트, 개수 응답")
    @GetMapping
    public ResponseEntity<ApiResponse<MyBoxPageResponse>> getMyBoxList(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                myBoxFacade.getMyBoxList(member)
        ));
    }

    @Operation(summary = "마이 박스 수정", description = "이름, 설명")
    @PatchMapping("/{boxId}")
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

    @Operation(summary = "마이 박스 삭제", description = "BoxMember 본인과 BoxContent 모두 삭제 <br>" +
            "로그 남김")
    @DeleteMapping("/{boxId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyBox(
            @AuthenticationPrincipal Member member,
            @PathVariable Long boxId
    ) {
        myBoxFacade.deleteMyBox(member, boxId);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
