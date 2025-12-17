package net.watchbox.domain.box.controller;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.facade.SharedBoxFacade;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/shared")
public class SharedBoxController {
    private final SharedBoxFacade sharedBoxFacade;

//    // 공유 박스에 컨텐츠 리스트 추가
//    @PostMapping("/{boxId}/contents/batch")
//    public ApiResponse<Void> addSharedBoxContentList(
//            @PathVariable Long boxId,
//            @RequestBody List<Long> contentIds,
//            @AuthenticationPrincipal Member member
//    ) {
//        sharedBoxFacade.addSharedBoxContentList(boxId, contentIds, member);
//        return ApiResponse.success();
//    }
//
//    // 공유 박스에 컨텐츠 추가
//    @PostMapping("/{boxId}/contents")
//    public ApiResponse<Void> addSharedBoxContent(
//            @PathVariable Long boxId,
//            @PathVariable Long contentId,
//            @AuthenticationPrincipal Member member
//    ) {
//        sharedBoxFacade.addSharedBoxContent(boxId, contentIds, member);
//        return ApiResponse.success();
//    }

    // 공유 박스에 마이 박스 컨텐츠 리스트 추가

    // 공유 박스의 컨텐츠 조회 ToDo: Selector 별로 필터링
//    @GetMapping("/{boxId}")

    // 공유 박스의 컨텐츠 삭제
//    @DeleteMapping("/{boxId}/{contentId}")
}
