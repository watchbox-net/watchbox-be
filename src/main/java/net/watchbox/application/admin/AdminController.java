package net.watchbox.application.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.watchbox.application.admin.dto.TmdbContentItem;
import net.watchbox.application.admin.dto.TmdbWatchStatusItem;
import net.watchbox.domain.box.dto.box.request.BoxCreateRequest;
import net.watchbox.domain.box.dto.box.response.BoxCreateResponse;
import net.watchbox.domain.member.dto.response.ProfileResponse;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "관리자 API")
public class AdminController {
    private final AdminFacade adminFacade;

    /**
     * 샘플 계정 데이터 확보 시나리오
     * 1. (SQL) 계정 생성 - 샘플 OauthAccount + Member 3명
     * 2. 박스 생성 - 메인 샘플 멤버 개인/공유 박스 몇개 생성 - 개인 박스 2개, 공유 박스 4개
     * 3. (SQL) 박스 초대 - 공유 박스들에 다른 멤버 2명 초대
     * 4. (SQL) 박스 초대 수락 - 다른 멤버들의 박스 초대 {수락, 거절, 대기} 골고루
     * 5. 박스 컨텐츠 추가 - 개인/공유 박스 모두 추가, 빈 박스도 포함 - 영화/시리즈/인물 골고루
     * 6. (SQL) 시청 기록 추가
     */

    @Operation(summary = "Box 생성")
    @PostMapping("/members/{nickname}/boxes")
    public ResponseEntity<ApiResponse<BoxCreateResponse>> createBox(
            @PathVariable String nickname,
            @RequestBody @Valid BoxCreateRequest request
            ) {
        return ResponseEntity.ok(
                ApiResponse.success(adminFacade.createBox(nickname, request))
        );
    }

    @Operation(summary = "Box Content List 일괄 추가", description = "WatchMediaType = {MOVIE, TV}")
    @PostMapping("/members/{nickname}/boxes/{boxId}/contents/batch")
    public ResponseEntity<ApiResponse<Void>> batchAddContentsToBox(
            @PathVariable String nickname,
            @PathVariable Long boxId,
            @RequestBody List<@Valid TmdbContentItem> request
    ) {
        adminFacade.batchAddContentsToBox(nickname, boxId, request);
        return null;
    }

    @Operation(summary = "Watch Status List Upsert",
            description = "WatchMediaType = {MOVIE, TV} <br>" + "WatchStatus = {COMPLETED, WATCHING, PLANNED, PAUSED}")
    @PostMapping("/members/{nickname}/records/watch-status/batch")
    public ResponseEntity<ApiResponse<Void>> batchUpsertWatchStatus(
            @PathVariable String nickname,
            @RequestBody List<@Valid TmdbWatchStatusItem> request
    ) {
        adminFacade.batchUpsertWatchStatus(nickname, request);
        return null;
    }

    @Operation(summary = "TMDB 컨텐츠 데이터 일괄 저장")
    @PostMapping("/contents/save")
    public ResponseEntity<ApiResponse<Void>> saveContents(
            @RequestBody List<@Valid TmdbContentItem> request
    ){
        adminFacade.saveContents(request);
        return null;
    }

    // ==
    @Operation(summary = "샘플 OauthAccount + Member + Box + BoxMember 생성", description = "OauthAccount, Member 생성")
    @PostMapping("/members/sample")
    public ResponseEntity<ApiResponse<ProfileResponse>> createSampleMember(
            @RequestParam String name,
            @RequestParam String nickname
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(adminFacade.createSampleMember(name, nickname))
        );
    }

    @Operation(summary = "Web Push 테스트 발송 (POC)",
            description = "대상 닉네임의 모든 등록된 브라우저 구독에 Web Push 발송. " +
                    "응답값은 실제 전송 성공한 구독 개수 (0이면 등록된 구독 없음 또는 모두 실패).")
    @PostMapping("/web-push/test")
    public ResponseEntity<ApiResponse<Integer>> sendWebPushTest(
            @RequestParam String nickname,
            @RequestParam String text
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(adminFacade.sendWebPushTest(nickname, text))
        );
    }
}
