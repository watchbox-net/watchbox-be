package net.watchbox.domain.record.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.dto.list.ContentPageResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.request.ContentRecordQueryRequest;
import net.watchbox.domain.record.facade.ContentRecordFacade;
import net.watchbox.global.dto.response.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
@Tag(name = "ContentRecord", description = "시청 기록 API")
public class ContentRecordController {
    private final ContentRecordFacade contentRecordFacade;

    @GetMapping
    public ResponseEntity<ApiResponse<ContentPageResponse>> getMyRecordedContentPage(
            @AuthenticationPrincipal Member member,
            @ParameterObject
            @ModelAttribute ContentRecordQueryRequest request
//            @Parameter(description = "정렬 기준", example = "RECENT_SAVED, RECENT_WATCHED, OLDEST_SAVED, OLDEST_WATCHED")
//            @RequestParam(defaultValue = "RECENT_SAVED") SortOrder sort,
//            @Parameter(description = "시청 상태", example = "COMPLETED, WATCHING, PLANNED, PAUSED, NONE")
//            @RequestParam(required = false) WatchStatus watchStatus,
//            @Parameter(description = "좋아요 여부 (true: 좋아요만 조회, false: 미관여)", example = "false")
//            @RequestParam(defaultValue = "false") Boolean liked
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                contentRecordFacade.getMyRecordedContentPage(member, request)
        ));
    }
}
