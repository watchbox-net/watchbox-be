package net.watchbox.domain.content.api;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.base.dto.detail.ContentDetailResponse;
import net.watchbox.domain.content.base.entity.MediaType;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ContentController {
    private final ContentFacade contentFacade;

    @Operation(summary = "콘텐츠 상세 조회 (사용자 기록 포함)")
    @GetMapping("/api/contents/{mediaType}/{contentId}/record")
    public ResponseEntity<ApiResponse<ContentDetailResponse>> getContentDetailWithRecord(
            @AuthenticationPrincipal Long memberId,
            @PathVariable MediaType mediaType,
            @PathVariable Long contentId
    ) {

        return ResponseEntity.ok(ApiResponse.success(
                contentFacade.getContentDetailWithRecord(memberId, mediaType, contentId)
        ));
    }

    @GetMapping("/api/contents/{mediaType}/{contentId})")
    public ResponseEntity<ApiResponse<ContentDetailResponse>> getContentDetail(
            @PathVariable MediaType mediaType,
            @PathVariable Long contentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                contentFacade.getContentDetail(mediaType, contentId)
        ));
    }
}
