package net.watchbox.domain.box.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.request.BoxContentRequest;
import net.watchbox.domain.box.dto.response.content.SharedBoxContentResponse;
import net.watchbox.domain.box.facade.SharedBoxContentFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/shared/{boxId}/contents")
@Tag(name = "SharedBoxContent", description = "SharedBoxContent API")
public class SharedBoxContentController {
    private final SharedBoxContentFacade sharedBoxContentFacade;

    // 공유 박스에 컨텐츠 추가
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addSharedBoxContent(
            @PathVariable("boxId") Long boxId,
            @RequestBody BoxContentRequest boxContentRequests,
            @AuthenticationPrincipal Member member
    ) {
        sharedBoxContentFacade.addSharedBoxContent(member, boxId, boxContentRequests);
        return ResponseEntity.status(201).body(ApiResponse.success());
    }

    // 공유 박스에 컨텐츠 리스트 추가
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> addSharedBoxContentList(
            @PathVariable("boxId") Long boxId,
            @RequestBody List<BoxContentRequest> boxContentRequests,
            @AuthenticationPrincipal Member member
    ) {
        // 빈 배열 검증
//        if (boxContentRequests.isEmpty()) {
//            throw new CustomException(ErrorCode.EMPTY_CHECKED_LIST);
//        }

        sharedBoxContentFacade.addSharedBoxContentList(member, boxId, boxContentRequests);
        return ResponseEntity.status(201).body(ApiResponse.success());
    }

    // 공유 박스에 마이 박스 컨텐츠 리스트 추가 (단일 포함)
    @PostMapping("/mine")
    public ResponseEntity<ApiResponse<Void>> addSharedBoxContentListFromMine(
            @PathVariable("boxId") Long boxId,
            @RequestBody List<Long> contentIds,
            @AuthenticationPrincipal Member member
    ) {
        sharedBoxContentFacade.addSharedBoxContentListFromMine(member, boxId, contentIds);
        return ResponseEntity.status(201).body(ApiResponse.success());
    }

    // (임시) 공유 박스의 컨텐츠 전체 조회 ToDo: Selector 별로 필터링
    @GetMapping
    public ResponseEntity<ApiResponse<SharedBoxContentResponse>> getSharedBoxContents(
            @PathVariable("boxId") Long boxId,
            @AuthenticationPrincipal Member member
    ) {
        SharedBoxContentResponse response = sharedBoxContentFacade.getSharedBoxContents(member, boxId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 공유 박스의 내 컨텐츠 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeSharedBoxContent(
            @PathVariable("boxId") Long boxId,
            @RequestBody Long sbcId,
            @AuthenticationPrincipal Member member
    ) {
        sharedBoxContentFacade.removeSharedBoxContent(member, boxId, sbcId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 공유 박스의 내 컨텐츠 리스트 삭제
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> removeSharedBoxContentList(
            @PathVariable("boxId") Long boxId,
            @RequestBody List<Long> sbcIds,
            @AuthenticationPrincipal Member member
    ) {
        sharedBoxContentFacade.removeSharedBoxContentList(member, boxId, sbcIds);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
