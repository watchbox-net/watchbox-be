package net.watchbox.domain.box.controller;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.request.BoxContentRequest;
import net.watchbox.domain.box.dto.response.my.MyBoxContentResponse;
import net.watchbox.domain.box.facade.MyBoxContentFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/my")
public class MyBoxContentController {
    private final MyBoxContentFacade myBoxContentFacade;

    // Todo: 다중 추가, 다중 삭제

    // 마이 박스에 컨텐츠 추가
    @PostMapping("/contents")
    public ResponseEntity<ApiResponse<Void>> addMyBoxContent(
            @AuthenticationPrincipal Member member,
            @RequestBody BoxContentRequest boxContentRequest
            ) {

        myBoxContentFacade.addMyBoxContent(member, boxContentRequest);
        return ResponseEntity.status(201).body(ApiResponse.success());
    }

    // (임시) 마이 박스 컨텐츠 리스트 조회 ToDo: 무한스크롤로 변경
    @GetMapping("/contents")
    public ResponseEntity<ApiResponse<MyBoxContentResponse>> getMyBoxContents(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(myBoxContentFacade.getMyBoxContents(member))
        );
    }

    // 마이 박스의 컨텐츠 삭제
    @DeleteMapping("/contents")
    public ResponseEntity<ApiResponse<Void>> removeMyBoxContent(
            @AuthenticationPrincipal Member member,
            @RequestParam Long contentId
    ) {
        myBoxContentFacade.removeMyBoxContent(member, contentId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
