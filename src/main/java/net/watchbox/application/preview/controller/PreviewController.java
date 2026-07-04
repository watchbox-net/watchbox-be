package net.watchbox.application.preview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.box.response.BoxPageResponse;
import net.watchbox.domain.box.dto.content.response.BoxContentCountResponse;
import net.watchbox.domain.box.dto.content.request.BoxContentCountRequest;
import net.watchbox.domain.box.dto.content.request.BoxContentQueryRequest;
import net.watchbox.domain.box.facade.box.BoxFacade;
import net.watchbox.domain.box.facade.content.BoxContentFacade;
import net.watchbox.domain.content.dto.list.ContentCursorPageResponse;
import net.watchbox.domain.member.dto.response.MyPageResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.member.facade.MemberFacade;
import net.watchbox.domain.member.service.MemberQueryService;
import net.watchbox.domain.record.dto.record.request.ContentRecordCountRequest;
import net.watchbox.domain.record.dto.record.request.ContentRecordQueryRequest;
import net.watchbox.domain.record.dto.record.response.ContentRecordCountResponse;
import net.watchbox.domain.record.facade.ContentRecordFacade;
import net.watchbox.global.dto.response.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/preview")
@Tag(name = "Preview", description = "샘플 화면 API")
public class PreviewController {
    private final static String SAMPLE_NICKNAME = "해달";
    private final MemberQueryService memberQueryService;

    private final BoxFacade boxFacade;
    private final BoxContentFacade boxContentFacade;
    private final ContentRecordFacade contentRecordFacade;
    private final MemberFacade memberFacade;

    /**
     * Preview 페이지 대상
     * 1. 박스 페이지
     * 2. 박스 컨텐츠 페이지
     * 3. 시청 기록 페이지
     * 4. 마이 페이지
     */
    @Operation(summary = "Preview 박스 페이지 조회")
    @GetMapping("/boxes")
    public ResponseEntity<ApiResponse<BoxPageResponse>> getBoxPage() {
        Member member = memberQueryService.getByNicknameOrThrow(SAMPLE_NICKNAME);
        return ResponseEntity.ok(ApiResponse.success(
                boxFacade.getBoxPage(member)
        ));
    }

    @Operation(summary = "Preview 박스 컨텐츠 페이지 조회")
    @GetMapping("/boxes/{boxId}/contents")
    public ResponseEntity<ApiResponse<ContentCursorPageResponse>> getBoxContentPage(
            @ParameterObject
            @ModelAttribute BoxContentQueryRequest request,
            @PathVariable("boxId") Long boxId
    ) {
        Member member = memberQueryService.getByNicknameOrThrow(SAMPLE_NICKNAME);
        return ResponseEntity.ok(
                ApiResponse.success(boxContentFacade.getBoxContentPage(member, request, boxId))
        );
    }

    @Operation(summary = "Preview 박스 컨텐츠 총 개수 조회")
    @GetMapping("/boxes/{boxId}/contents/count")
    public ResponseEntity<ApiResponse<BoxContentCountResponse>> getBoxContentCount(
            @ParameterObject
            @ModelAttribute BoxContentCountRequest request,
            @PathVariable("boxId") Long boxId
    ) {
        Member member = memberQueryService.getByNicknameOrThrow(SAMPLE_NICKNAME);
        return ResponseEntity.ok(
                ApiResponse.success(boxContentFacade.getBoxContentCount(member, request, boxId))
        );
    }

    @Operation(summary = "Preview 시청 기록 조회")
    @GetMapping("/records/watch")
    public ResponseEntity<ApiResponse<ContentCursorPageResponse>> getMyRecordedContentPage(
            @ParameterObject
            @ModelAttribute ContentRecordQueryRequest request
    ) {
        Member member = memberQueryService.getByNicknameOrThrow(SAMPLE_NICKNAME);
        return ResponseEntity.ok(ApiResponse.success(
                contentRecordFacade.getMyRecordedContentPage(member, request)
        ));
    }

    @Operation(summary = "Preview 시청 기록 총 개수 조회")
    @GetMapping("/records/watch/count")
    public ResponseEntity<ApiResponse<ContentRecordCountResponse>> getMyContentRecordCount(
            @ParameterObject
            @ModelAttribute ContentRecordCountRequest request
    ) {
        Member member = memberQueryService.getByNicknameOrThrow(SAMPLE_NICKNAME);
        return ResponseEntity.ok(ApiResponse.success(
                contentRecordFacade.getMyContentRecordCount(member, request)
        ));
    }

    @Operation(summary = "Preview 마이 페이지 조회", description = "프로필 정보와 멤버 컨텐츠 개수 조회")
    @GetMapping("/members/mypage")
    public ResponseEntity<ApiResponse<MyPageResponse>> getMyPage(){
        Member member = memberQueryService.getByNicknameOrThrow(SAMPLE_NICKNAME);
        return ResponseEntity.ok(
                ApiResponse.success(memberFacade.getMyPage(member))
        );
    }
}
