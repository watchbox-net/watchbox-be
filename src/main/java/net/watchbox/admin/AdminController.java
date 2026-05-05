package net.watchbox.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.watchbox.admin.dto.TmdbContentItem;
import net.watchbox.admin.dto.TmdbWatchStatusItem;
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

    @Operation(summary = "샘플 계정 생성")
    @PostMapping("/members/sample")
    public ResponseEntity<ApiResponse<Void>> createSampleMember(
            @RequestParam Long id,
            @RequestParam String name,
            @RequestParam String email
    ) {
        adminFacade.createSampleMember(id, name, email);
        return null;
    }

    @Operation(summary = "멤버에 대해 Content 리스트 일괄 Box에 추가", description = "WatchMediaType = {MOVIE, TV}")
    @PostMapping("/members/{memberId}/boxes/{boxId}/contents/batch")
    public ResponseEntity<ApiResponse<Void>> batchAddContentsToBox(
            @PathVariable Long memberId,
            @PathVariable Long boxId,
            @RequestBody List<@Valid TmdbContentItem> request
    ) {
        adminFacade.batchAddContentsToBox(memberId, boxId, request);
        return null;
    }

    @Operation(summary = "멤버에 대해 {Content x WatchStatus} 리스트 일괄 변경",
            description = "WatchMediaType = {MOVIE, TV} <br>" + "WatchStatus = {COMPLETED, WATCHING, PLANNED, PAUSED}")
    @PostMapping("/members/{memberId}/records/watch-status/batch")
    public ResponseEntity<ApiResponse<Void>> batchUpsertWatchStatus(
            @PathVariable Long memberId,
            @RequestBody List<@Valid TmdbWatchStatusItem> request
    ) {
        adminFacade.batchUpsertWatchStatus(memberId, request);
        return null;
    }

}
