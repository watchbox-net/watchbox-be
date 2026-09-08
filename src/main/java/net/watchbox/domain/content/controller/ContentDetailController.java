package net.watchbox.domain.content.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.facade.ContentDetailFacade;
import net.watchbox.domain.content.dto.detail.ContentDetailResponse;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contents/{mediaType}/{tmdbId}")
@Tag(name = "ContentDetail", description = "콘텐츠 상세 조회 API")
public class ContentDetailController {
    private final ContentDetailFacade contentDetailFacade;

    @Operation(summary = "콘텐츠 상세 페이지 조회", description = "비로그인/로그인 분기 있음")
    @GetMapping
    public ResponseEntity<ApiResponse<ContentDetailResponse>> getContentDetail(
            @PathVariable MediaType mediaType,
            @PathVariable Long tmdbId,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                contentDetailFacade.getContentDetail(mediaType, tmdbId, member)
        ));
    }
}
