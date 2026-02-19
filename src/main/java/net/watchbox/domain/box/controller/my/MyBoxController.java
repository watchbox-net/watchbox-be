package net.watchbox.domain.box.controller.my;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.BoxCreateRequest;
import net.watchbox.domain.box.dto.BoxCreateResponse;
import net.watchbox.domain.box.dto.MyBoxListResponse;
import net.watchbox.domain.box.dto.MyBoxResponse;
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
// ToDo: Swagger로 확인하기
public class MyBoxController {
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

    // ToDo: 마이 박스 수정하기
    // ToDo: 마이 박스 삭제하기
}
