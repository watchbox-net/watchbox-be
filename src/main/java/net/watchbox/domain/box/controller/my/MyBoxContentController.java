package net.watchbox.domain.box.controller.my;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.BoxContentAddRequest;
import net.watchbox.domain.box.dto.BoxContentAddResponse;
import net.watchbox.domain.box.dto.response.my.MyBoxContentListResponse;
import net.watchbox.domain.box.facade.my.MyBoxContentFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/my/contents")
@Tag(name = "MyBox Content")
public class MyBoxContentController {
    private final MyBoxContentFacade myBoxContentFacade;

    // Todo: 다중 추가, 다중 삭제

    @Operation(summary = "마이 박스에 컨텐츠 추가")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "추가 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "박스에 이미 추가된 컨텐츠",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<BoxContentAddResponse>> addMyBoxContent(
            @AuthenticationPrincipal Member member,
            @RequestBody BoxContentAddRequest request
            ) {
        return ResponseEntity.status(201).body(
                ApiResponse.success(myBoxContentFacade.addMyBoxContent(member, request))
        );
    }

    // (임시) 마이 박스 컨텐츠 리스트 조회 ToDo: 무한스크롤로 변경
    @GetMapping
    public ResponseEntity<ApiResponse<MyBoxContentListResponse>> getMyBoxContents(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(myBoxContentFacade.getMyBoxContents(member))
        );
    }

    @Operation(summary = "마이 박스 컨텐츠 삭제")
    @DeleteMapping("/{boxContentId}")
    public ResponseEntity<ApiResponse<Void>> removeMyBoxContent(
            @RequestParam Long boxContentId
    ) {
        myBoxContentFacade.removeMyBoxContent(boxContentId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
