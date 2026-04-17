package net.watchbox.domain.content.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.dto.box.ContentBoxDiffRequest;
import net.watchbox.domain.content.dto.box.ContentBoxSheetResponse;
import net.watchbox.domain.content.dto.box.ContentBoxUpdateResponse;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.facade.ContentBoxFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contents/{mediaType}/{tmdbId}/boxes")
@Tag(name = "ContentBox", description = "컨텐츠 기준 박스 포함 여부 조회 및 일괄 추가/삭제 API")
public class ContentBoxController {
    private final ContentBoxFacade contentBoxFacade;

    @Operation(summary = "컨텐츠가 사용자 박스들에 저장되어있는지 유무 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<ContentBoxSheetResponse>> getContentBoxSheet(
            @AuthenticationPrincipal Member member,
            @PathVariable MediaType mediaType,
            @PathVariable Long tmdbId
            ) {
        return ResponseEntity.ok(ApiResponse.success(
                contentBoxFacade.getContentBoxSheet(member, tmdbId, mediaType)
        ));
    }

    @Operation(summary = "컨텐츠를 사용자 박스들에 일괄 추가/삭제")
    @PostMapping
    public ResponseEntity<ApiResponse<ContentBoxUpdateResponse>> updateContentBoxes(
            @AuthenticationPrincipal Member member,
            @PathVariable MediaType mediaType,
            @PathVariable Long tmdbId,
            @RequestBody ContentBoxDiffRequest request
    ){
        return ResponseEntity.ok(ApiResponse.success(
                contentBoxFacade.updateContentBoxes(member, tmdbId, mediaType, request)
        ));
    }
}
