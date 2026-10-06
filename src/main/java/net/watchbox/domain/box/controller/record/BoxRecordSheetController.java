package net.watchbox.domain.box.controller.record;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.box.dto.record.request.BoxRecordDiffRequest;
import net.watchbox.domain.box.dto.record.request.BoxRecordSheetQueryRequest;
import net.watchbox.domain.box.dto.record.response.BoxRecordUpdateResponse;
import net.watchbox.domain.box.facade.record.BoxRecordSheetFacade;
import net.watchbox.domain.content.dto.list.ContentCursorPageResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boxes/{boxId}/records")
@Tag(name = "4-5 [Box] Record Sheet")
@Observed
public class BoxRecordSheetController {

    private final BoxRecordSheetFacade boxRecordSheetFacade;

    @Operation(summary = "시청 기록 목록 + 이 박스 포함 여부 조회",
            description = "내 시청 기록을 커서 기반 무한스크롤로 조회하고, 각 콘텐츠가 이 박스에 담겨 있는지를 "
                    + "<code>hasAddedInbox</code> 로 함께 내려준다. "
                    + "정렬은 최근 기록순 고정이며, 미디어 타입 탭(전체/영화/시리즈)과 시청 상태 필터를 지원한다. "
                    + "공유 박스에서는 <b>내가 담은 것만</b> 포함으로 본다 — 남이 담은 콘텐츠는 삭제 권한이 없어 "
                    + "체크 상태로 보여주면 체크를 풀 수 없다.")
    @GetMapping
    public ResponseEntity<ApiResponse<ContentCursorPageResponse>> getRecordSheet(
            @AuthenticationPrincipal Member member,
            @PathVariable("boxId") Long boxId,
            @ParameterObject
            @ModelAttribute BoxRecordSheetQueryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                boxRecordSheetFacade.getRecordSheet(member, boxId, request)
        ));
    }

    @Operation(summary = "체크한 콘텐츠를 박스에 일괄 추가/삭제",
            description = "화면에서 체크/해제한 것만 보낸다(contentId 기준). 이미 담긴 것을 또 추가하거나 "
                    + "내가 담지 않은 것을 삭제 요청하면 예외 없이 건너뛰므로, <b>요청 목록과 응답 목록이 다를 수 있다.</b> "
                    + "응답의 addedContentIds / removedContentIds 를 기준으로 체크 상태를 맞추면 된다. "
                    + "이 API 는 시청 기록을 변경하지 않는다.")
    @PostMapping
    public ResponseEntity<ApiResponse<BoxRecordUpdateResponse>> updateBoxRecords(
            @AuthenticationPrincipal Member member,
            @PathVariable("boxId") Long boxId,
            @RequestBody BoxRecordDiffRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                boxRecordSheetFacade.updateBoxRecords(member, boxId, request)
        ));
    }
}
