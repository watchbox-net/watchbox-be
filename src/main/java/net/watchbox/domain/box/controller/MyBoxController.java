package net.watchbox.domain.box.controller;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.response.MyBoxContentResponse;
import net.watchbox.domain.box.facade.MyBoxFacade;
import net.watchbox.domain.content.common.entity.MediaType;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/my")
public class MyBoxController {
    private final MyBoxFacade myBoxFacade;

    // 마이 박스에 컨텐츠 추가
    @PostMapping("/contents")
    public ApiResponse<Void> addMyBoxContent(
            @AuthenticationPrincipal Member member,
            @RequestParam Long contentId,
            @RequestParam MediaType mediaType
            ) {

        myBoxFacade.addMyBoxContent(member, contentId, mediaType);
        return ApiResponse.success();
    }

    // 마이 박스 컨텐츠 리스트 조회 ToDo: 무한스크롤로 변경
    @GetMapping("/contents")
    public ApiResponse<MyBoxContentResponse> getMyBoxContents(
            @AuthenticationPrincipal Member member
    ) {
        return ApiResponse.success(
                myBoxFacade.getMyBoxContents(member)
        );
    }

    // 마이 박스에 컨텐츠 삭제
    @DeleteMapping("/contents")
    public ApiResponse<Void> removeMyBoxContent(
            @AuthenticationPrincipal Member member,
            @RequestParam Long contentId
    ) {
        myBoxFacade.removeMyBoxContent(member, contentId);
        return ApiResponse.success();
    }
}
