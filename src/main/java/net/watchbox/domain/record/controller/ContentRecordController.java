package net.watchbox.domain.record.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.dto.list.ContentCursorPageResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.record.request.ContentLikeUpsertRequest;
import net.watchbox.domain.record.dto.record.request.ContentRecordCountRequest;
import net.watchbox.domain.record.dto.record.request.ContentRecordQueryRequest;
import net.watchbox.domain.record.dto.record.request.WatchStatusUpsertRequest;
import net.watchbox.domain.record.dto.record.response.ContentRecordCountResponse;
import net.watchbox.domain.record.dto.record.response.ContentRecordResponse;
import net.watchbox.domain.record.facade.ContentRecordFacade;
import net.watchbox.global.dto.response.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
@Tag(name = "ContentRecord", description = "컨텐츠 기록 API")
public class ContentRecordController {
    private final ContentRecordFacade contentRecordFacade;

    /**
     # 시청 기록 조회
     시청 기록 단일 조회 @GetMapping("/{recordId}") -> DevController
     시청 기록 조회

     # 시청 상태
     시청 상태 등록/변경 @PostMapping("/status") body: { contentId, mediaType, watchStatus }
     시청 상태 삭제 @DeleteMapping("/status/{recordId}")

     # 좋아요
     좋아요 등록한 리스트 조회 @GetMapping("/likes")
     좋아요 등록/변경 @PostMapping("/likes") body: { contentId, mediaType, watchStatus }
     좋아요 삭제 @DeleteMapping("/likes/{recordId}")

     ! 시청 상태 삭제 & 좋아요 기록 없음 -> 시청 기록 삭제
     ! 둘 중 어떤 기록을 남기든 DB에 해당 Content 없으면 새로 저장
     ! 시청 상태는 MOVIE, TV 까지만 취급
     ! 좋아요는 MOVIE, TV, PERSON 모두 취급
     */

    @Operation(summary = "시청 기록 조회", description = "정렬 & 필터 & 커서 기반 무한스크롤 조회")
    @GetMapping("/watch")
    public ResponseEntity<ApiResponse<ContentCursorPageResponse>> getMyRecordedContentPage(
            @AuthenticationPrincipal Member member,
            @ParameterObject
            @ModelAttribute ContentRecordQueryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                contentRecordFacade.getMyRecordedContentPage(member, request)
        ));
    }

    @Operation(summary = "시청 기록 총 개수 조회")
    @GetMapping("/watch/count")
    public ResponseEntity<ApiResponse<ContentRecordCountResponse>> getMyContentRecordCount(
            @AuthenticationPrincipal Member member,
            @ParameterObject
            @ModelAttribute ContentRecordCountRequest request
            ) {
        return ResponseEntity.ok(ApiResponse.success(
                contentRecordFacade.getMyContentRecordCount(member, request)
        ));
    }

    // ─────────────────────────────────────── 시청 상태 쓰기 ───────────────────────────────────────

    @Operation(summary = "시청 상태 등록/변경", description = "WatchMediaType = {MOVIE, TV} <br>" +
            "WatchStatus = {COMPLETED, WATCHING, PLANNED, PAUSED}")
    @PostMapping("/watch/status")
    public ResponseEntity<ApiResponse<ContentRecordResponse>> upsertWatchStatus(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid WatchStatusUpsertRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                contentRecordFacade.upsertWatchStatus(member, request))
        );
    }

    @Operation(summary = "시청 상태 기록 삭제")
    @DeleteMapping("/{recordId}/watch/status")
    public ResponseEntity<ApiResponse<Void>> deleteWatchStatus(
            @AuthenticationPrincipal Member member,
            @PathVariable Long recordId
    ) {
        contentRecordFacade.deleteWatchStatus(member, recordId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ─────────────────────────────────────── 좋아요 업데이트 ───────────────────────────────────────

    @Operation(summary = "좋아요 등록/변경")
    @PostMapping("/likes")
    public ResponseEntity<ApiResponse<ContentRecordResponse>> createContentLike(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid ContentLikeUpsertRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                contentRecordFacade.upsertContentLike(member, request))
        );
    }

    // 현재 미사용중
    @Operation(summary = "좋아요 삭제")
    @DeleteMapping("/{recordId}/likes")
    public ResponseEntity<ApiResponse<Void>> deleteContentLike(
            @AuthenticationPrincipal Member member,
            @PathVariable Long recordId
    ) {
        contentRecordFacade.deleteContentLike(member, recordId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ─────────────────────────────────────── 시청 기록 히스토리  ───────────────────────────────────────
//    @Operation(summary = "시청 기록 히스토리 페이지 조회", description = "커서 기반 무한 스크롤 조회")
//    @GetMapping("/history")
//    public ResponseEntity<ApiResponse<ContentRecordHistoryPageResponse>> getMyContentRecordHistoryPage(
//            @AuthenticationPrincipal Member member,
}
