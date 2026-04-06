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
import net.watchbox.domain.box.facade.content.SharedBoxContentFacade;
import net.watchbox.domain.content.base.dto.list.ContentPageResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/shared/{boxId}/contents")
@Tag(name = "SharedBoxContent", description = "공유 박스 컨텐츠 API")
@Observed
public class SharedBoxContentController {
    private final SharedBoxContentFacade sharedBoxContentFacade;

    @Operation(summary = "공유 박스에 컨텐츠 추가")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "추가 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "박스 컨텐츠 편집 권한 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "박스에 이미 추가된 컨텐츠",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<BoxContentAddResponse>> addSharedBoxContent(
            @AuthenticationPrincipal Member member,
            @PathVariable("boxId") Long boxId,
            @RequestBody BoxContentAddRequest request
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(
                sharedBoxContentFacade.addSharedBoxContent(member, boxId, request)
        ));
    }

    // 공유 박스에 컨텐츠 리스트 추가
//    @PostMapping("/batch")
//    public ResponseEntity<ApiResponse<Void>> addSharedBoxContentList(
//            @PathVariable("boxId") Long boxId,
//            @RequestBody List<BoxContentAddRequest> request,
//            @AuthenticationPrincipal Member member
//    ) {
//        // 빈 배열 검증
////        if (boxContentRequests.isEmpty()) {
////            throw new CustomException(ErrorCode.EMPTY_CHECKED_LIST);
////        }
//
//        sharedBoxContentFacade.addSharedBoxContentList(member, boxId, request);
//        return ResponseEntity.status(201).body(ApiResponse.success());
//    }

//    // 공유 박스에 마이 박스 컨텐츠 리스트 추가 (단일 포함)
//    @PostMapping("/mine")
//    public ResponseEntity<ApiResponse<Void>> addSharedBoxContentListFromMine(
//            @PathVariable("boxId") Long boxId,
//            @RequestBody List<Long> contentIds,
//            @AuthenticationPrincipal Member member
//    ) {
//        sharedBoxContentFacade.addSharedBoxContentListFromMine(member, boxId, contentIds);
//        return ResponseEntity.status(201).body(ApiResponse.success());
//    }

    // ToDo: Selector 별로 필터링
    @Operation(summary = "공유 박스 컨텐츠 리스트 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<ContentPageResponse>> getSharedBoxContents(
            @AuthenticationPrincipal Member member,
            @PathVariable("boxId") Long boxId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedBoxContentFacade.getSharedBoxContents(member, boxId)
        ));
    }

    @Operation(summary = "공유 박스에서 내 컨텐츠 삭제")
    @DeleteMapping("/{boxContentId}")
    public ResponseEntity<ApiResponse<Void>> removeSharedBoxContent(
            @AuthenticationPrincipal Member member,
            @PathVariable("boxId") Long boxId,
            @RequestParam Long boxContentId
    ) {
        sharedBoxContentFacade.removeSharedBoxContent(member, boxId, boxContentId);
        return ResponseEntity.ok(ApiResponse.success());
    }

//    // 공유 박스의 내 컨텐츠 리스트 삭제
//    @DeleteMapping("/batch")
//    public ResponseEntity<ApiResponse<Void>> removeSharedBoxContentList(
//            @PathVariable("boxId") Long boxId,
//            @RequestBody List<Long> sbcIds,
//            @AuthenticationPrincipal Member member
//    ) {
//        sharedBoxContentFacade.removeSharedBoxContentList(member, boxId, sbcIds);
//        return ResponseEntity.ok(ApiResponse.success());
//    }
}
