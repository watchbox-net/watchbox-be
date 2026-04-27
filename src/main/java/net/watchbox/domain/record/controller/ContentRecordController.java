package net.watchbox.domain.record.controller;

import io.swagger.v3.oas.annotations.Operation;
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

    // ToDo: 무한스크롤
    @Operation(summary = "내 시청 기록 조회", description = "정렬 & 필터 & 무한스크롤 시청 기록 페이지 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<ContentPageResponse>> getMyRecordedContentPage(
            @AuthenticationPrincipal Member member,
            @ParameterObject
            @ModelAttribute ContentRecordQueryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                contentRecordFacade.getMyRecordedContentPage(member, request)
        ));
    }
}
