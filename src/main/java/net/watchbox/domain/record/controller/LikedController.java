package net.watchbox.domain.record.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.base.dto.list.ContentPageResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.dto.request.ContentLikeUpsertRequest;
import net.watchbox.domain.record.dto.response.ContentRecordResponse;
import net.watchbox.domain.record.facade.ContentRecordFacade;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records/likes")
@Tag(name = "Liked", description = "좋아요 API")
public class LikedController {
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

    @Operation(summary = "좋아요 표시된 시청 기록 리스트 조회",
            description = "좋아요는 시청 상태와 달리 PERSON도 포함하여 조회 <br>"+
                    "좋아요만 포함, 싫어요는 미포함")
    @GetMapping
    public ResponseEntity<ApiResponse<ContentPageResponse>> getContentLikeList(
            @AuthenticationPrincipal Member member
//            @RequestParam(value = "mediaType", required = false) String mediaType,
//            @RequestParam(value = "page", defaultValue = "0") int page,
//            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                contentRecordFacade.getContentLikeList(member)
        ));
    }

    @Operation(summary = "좋아요 등록/변경")
    @PostMapping
    public ResponseEntity<ApiResponse<ContentRecordResponse>> createContentLike(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid ContentLikeUpsertRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                contentRecordFacade.upsertContentLike(member, request))
        );
    }

    @Operation(summary = "좋아요 삭제")
    @DeleteMapping("/{recordId}")
    public ResponseEntity<ApiResponse<Void>> deleteContentLike(
            @AuthenticationPrincipal Member member,
            @PathVariable Long recordId
    ) {
        contentRecordFacade.deleteContentLike(member, recordId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
