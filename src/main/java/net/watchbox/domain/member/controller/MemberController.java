package net.watchbox.domain.member.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.member.dto.response.MemberSearchPageResponse;
import net.watchbox.domain.member.service.MemberService;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {
    private final MemberService memberService;

    // ToDo: 공유박스ID 파라미터로 들어오면 해당 공유박스에 초대된 멤버인지 아닌지 정보도 함께 전송
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<MemberSearchPageResponse>> searchMemberList(
            @RequestParam String keyword
    ){
        return ResponseEntity.ok(
                ApiResponse.success(memberService.searchMemberList(keyword))
        );
    }

    // ToDo: 마이 페이지 응답
    // 프로필 정보, 컨텐츠 개수s
}
