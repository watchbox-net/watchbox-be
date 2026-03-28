package net.watchbox.domain.content.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.content.base.dto.detail.ContentDetailResponse;
import net.watchbox.domain.content.base.entity.MediaType;
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
@RequestMapping("/api/contents")
@Tag(name = "Content Detail", description = "컨텐츠 상세 조회 API")
@Slf4j
public class ContentController {
    private final ContentFacade contentFacade;

    @Operation(summary = "컨텐츠 상세 페이지 조회")
    @GetMapping("/{mediaType}/{contentId}")
    public ResponseEntity<ApiResponse<ContentDetailResponse>> getContentDetail(
            @AuthenticationPrincipal Member member,
            @PathVariable MediaType mediaType,
            @PathVariable Long contentId
    ) {
        Long memberId = member != null ? member.getMemberId() : null;
        if(memberId == null) {
            log.info("memberId is null");
            return ResponseEntity.ok(ApiResponse.success(
                    contentFacade.getContentDetail(mediaType, contentId)
            ));
        }

        log.info("memberId is {}", memberId);
        return ResponseEntity.ok(ApiResponse.success(
//                contentFacade.getContentDetail(mediaType, contentId)
                contentFacade.getContentDetailWithRecord(memberId, mediaType, contentId)
        ));
    }
}
