package net.watchbox.domain.box.controller;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.request.BoxContentRequest;
import net.watchbox.domain.box.facade.SharedBoxContentFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/shared")
public class SharedBoxContentController {
    private final SharedBoxContentFacade sharedBoxContentFacade;

    // 공유 박스에 컨텐츠 추가
    @PostMapping("/{boxId}/contents")
    public ApiResponse<Void> addSharedBoxContent(
            @PathVariable Long boxId,
            @RequestBody BoxContentRequest boxContentRequests,
            @AuthenticationPrincipal Member member
    ) {
        sharedBoxContentFacade.addSharedBoxContent(member, boxId, boxContentRequests);
        return ApiResponse.success();
    }

    // 공유 박스에 컨텐츠 리스트 추가
    @PostMapping("/{boxId}/contents/batch")
    public ApiResponse<Void> addSharedBoxContentList(
            @PathVariable Long boxId,
            @RequestBody List<BoxContentRequest> boxContentRequests,
            @AuthenticationPrincipal Member member
    ) {
        // 빈 배열 검증
        if (boxContentRequests.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_CHECKED_LIST);
        }

        sharedBoxContentFacade.addSharedBoxContentList(member, boxId, boxContentRequests);
        return ApiResponse.success();
    }


    // 공유 박스에 마이 박스 컨텐츠 리스트 추가
    @PostMapping("/{boxId}/contents/mine")
    public ApiResponse<Void> addSharedBoxContentListFromMine(
            @PathVariable Long boxId,
            @RequestBody List<Long> contentIds, // 내 박스에 추가된 컨텐츠들은 이미 DB에 저장되어 있으므로 contentId 리스트만 받음
            @AuthenticationPrincipal Member member
    ) {
        // 빈 배열 검증
        if (contentIds.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_CHECKED_LIST);
        }

        sharedBoxContentFacade.addSharedBoxContentListFromMine(member, boxId, contentIds);
        return ApiResponse.success();
    }

    // 공유 박스의 컨텐츠 전체 조회 ToDo: Selector 별로 필터링
//    @GetMapping("/{boxId}")

    // 공유 박스의 내 컨텐츠 삭제
//    @DeleteMapping("/{boxId}/{contentId}")
}
