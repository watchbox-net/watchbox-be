package net.watchbox.domain.box.controller.content;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.content.BoxContentAddRequest;
import net.watchbox.domain.box.dto.content.BoxContentAddResponse;
import net.watchbox.domain.box.facade.content.BoxContentFacade;
import net.watchbox.domain.content.base.dto.list.ContentPageResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/{boxId}/contents")
@Tag(name = "BoxContent", description = "박스 컨텐츠 API")
@Observed
public class BoxContentController {
    private final BoxContentFacade boxContentFacade;

    @Operation(summary = "박스 컨텐츠 리스트 조회", description = "정렬 최근순 (createdAt desc)")
    @GetMapping
    public ResponseEntity<ApiResponse<ContentPageResponse>> getBoxContentPage(
            @AuthenticationPrincipal Member member,
            @PathVariable("boxId") Long boxId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(boxContentFacade.getBoxContentPage(member, boxId))
        );
    }

    @Operation(summary = "박스에 컨텐츠 추가")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "추가 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "공유 박스 컨텐츠 편집 권한 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "박스에 이미 추가된 컨텐츠",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<BoxContentAddResponse>> addBoxContent(
            @AuthenticationPrincipal Member member,
            @PathVariable("boxId") Long boxId,
            @RequestBody BoxContentAddRequest request
    ){
        return ResponseEntity.status(201).body(
                ApiResponse.success(boxContentFacade.addBoxContent(member, boxId, request)
        ));
    }

    @Operation(summary = "박스에서 내 컨텐츠 삭제")
    @DeleteMapping("/{boxContentId}")
    public ResponseEntity<ApiResponse<Void>> removeBoxContent(
            @AuthenticationPrincipal Member member,
            @PathVariable("boxId") Long boxId,
            @PathVariable("boxContentId") Long boxContentId
    ) {
        boxContentFacade.removeBoxContent(member, boxId, boxContentId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
