package net.watchbox.domain.record.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.entity.WatchStatus;
import net.watchbox.global.dto.request.SortOrder;
import net.watchbox.domain.record.dto.response.ContentRecordResponse;
import net.watchbox.domain.record.facade.ContentRecordFacade;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
@Tag(name = "ContentRecord", description = "시청 기록 API")
public class ContentRecordController {
    private final ContentRecordFacade contentRecordFacade;

    @GetMapping
    public ResponseEntity<ApiResponse<ContentRecordResponse>> getContentRecordList(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "정렬 기준", example = "RECENT_SAVED, RECENT_WATCHED, OLDEST_SAVED, OLDEST_WATCHED")
            @RequestParam(defaultValue = "RECENT_SAVED") SortOrder sort,
            @Parameter(description = "시청 상태", example = "COMPLETED, WATCHING, PLANNED, PAUSED, NONE")
            @RequestParam(required = false) WatchStatus watchStatus,
            @Parameter(description = "좋아요 여부 (true: 좋아요만 조회, false: 미관여)", example = "false")
            @RequestParam(defaultValue = "false") Boolean liked
    ) {
//        if(liked && watchStatus != null ) {
//            예외처리
//        }

        return ResponseEntity.ok(ApiResponse.success(
                contentRecordFacade.getContentRecordList()
        ));
    }
}
