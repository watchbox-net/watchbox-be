package net.watchbox.domain.record.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.base.dto.list.ContentPageResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.request.WatchStatusUpsertRequest;
import net.watchbox.domain.record.dto.response.ContentRecordResponse;
import net.watchbox.domain.record.facade.ContentRecordFacade;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records/status")
@Tag(name = "WatchStatus", description = "시청 상태 API")
@Observed
public class WatchStatusController {
    private final ContentRecordFacade contentRecordFacade;
    /**
     # 시청 상태
     시청 상태 등록한 리스트 조회 @GetMapping("/status")
     시청 상태 등록/변경 @PostMapping("/status") body: { contentId, mediaType, watchStatus }
     시청 상태 삭제 @DeleteMapping("/status/{recordId}")

     # 좋아요
     좋아요 등록한 리스트 조회 @GetMapping("/likes")
     좋아요 등록/변경 @PostMapping("/likes") body: { contentId, mediaType, watchStatus }
     좋아요 삭제 @DeleteMapping("/likes/{recordId}")

     # 시청 상태 조회
     시청 기록 단일 조회 @GetMapping("/{recordId}") -> DevController

     ! 시청 상태 삭제 & 좋아요 기록 없음 -> 시청 기록 삭제
     ! 둘 중 어떤 기록을 남기든 DB에 해당 Content 없으면 새로 저장
     ! 시청 상태는 MOVIE, TV 까지만 취급
     ! 좋아요는 MOVIE, TV, PERSON 모두 취급
     */

    @Operation(summary = "시청 상태 등록된 시청 기록 리스트 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<ContentPageResponse>> getWatchStatusList(
            @AuthenticationPrincipal Member member
//            @RequestParam(value = "mediaType", required = false) String mediaType,
//            @RequestParam(value = "watchStatus", required = false) WatchStatus watchStatus,
//            @RequestParam(value = "page", defaultValue = "0") int page,
//            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                contentRecordFacade.getWatchStatusList(member)
//                watchRecordFacade.getWatchStatusList(member, mediaType, watchStatus, page, size))
        ));
    }

    @Operation(summary = "시청 상태 등록/변경", description = "WatchMediaType = {MOVIE, TV} <br>" +
            "WatchStatus = {COMPLETED, WATCHING, PLANNED, PAUSED}")
    @PostMapping
    public ResponseEntity<ApiResponse<ContentRecordResponse>> upsertWatchStatus(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid WatchStatusUpsertRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                contentRecordFacade.upsertWatchStatus(member, request))
        );
    }

    @Operation(summary = "시청 상태 삭제")
    @DeleteMapping("/{recordId}")
    public ResponseEntity<ApiResponse<Void>> deleteWatchStatus(
            @AuthenticationPrincipal Member member,
            @PathVariable Long recordId
    ) {
        contentRecordFacade.deleteWatchStatus(member, recordId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
