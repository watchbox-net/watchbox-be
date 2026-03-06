package net.watchbox.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.member.dto.response.MyPageResponse;
import net.watchbox.domain.member.dto.response.search.MemberSearchPageResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.facade.MemberFacade;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {
    private final MemberFacade memberFacade;

    @Operation(summary = "멤버 검색 API", description = "검색어와 공유박스ID로 멤버 리스트 조회 <br>" +
            "박스 멤버 상태 포함")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<MemberSearchPageResponse>> searchMemberList(
            @RequestParam String keyword,
            @RequestParam Long boxId
    ){
        return ResponseEntity.ok(
                ApiResponse.success(memberFacade.searchMemberListWithSharedStatus(keyword, boxId))
        );
    }

    // ToDo: 프로필 수정
//    @PatchMapping("/profile")

    // ToDo: 마이 페이지 응답
    @Operation(summary = "마이 페이지 조회 API", description = "프로필 정보와 멤버 컨텐츠 개수 조회")
    @GetMapping("/mypage")
    public ResponseEntity<ApiResponse<MyPageResponse>> getMyPage(
            @AuthenticationPrincipal Member member
    ){
        return ResponseEntity.ok(
                ApiResponse.success(memberFacade.getMyPage(member))
        );
    }

}
