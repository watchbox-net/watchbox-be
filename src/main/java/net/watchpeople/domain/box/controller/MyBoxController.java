package net.watchpeople.domain.box.controller;

import lombok.RequiredArgsConstructor;
import net.watchpeople.domain.box.facade.MyBoxFacade;
import net.watchpeople.domain.member.entity.Member;
import net.watchpeople.global.dto.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/my")
public class MyBoxController {
    private final MyBoxFacade myBoxFacade;

    // 마이 박스 조회

    // 마이 박스에 컨텐츠 추가 / 삭제
    @PostMapping("/contents")
    public ApiResponse<Void> addMyBoxContent(
            @AuthenticationPrincipal Member member,
            @RequestParam Long tmdbId
    ) {
        myBoxFacade.addMyBoxContent(member, tmdbId);
        return ApiResponse.success();
    }
}
