package net.watchbox.domain.content.api;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.base.dto.detail.ContentDetailResponse;
import net.watchbox.domain.content.base.dto.detail.DataSourceParam;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ContentController {
    private final ContentFacade contentFacade;

    @GetMapping("/api/contents/{mediaType}/{contentId}")
    public ResponseEntity<ApiResponse<ContentDetailResponse>> getContentDetail(
            @PathVariable MediaType mediaType,
            @PathVariable Long contentId,
            @RequestParam DataSourceParam source
    ) {

        switch (source) {
            case LOCAL:
                return ResponseEntity.ok(
                        ApiResponse.success(contentFacade.getContentDetailByDB(mediaType, contentId, source))
                );
            case REMOTE:
                return ResponseEntity.ok(
                        ApiResponse.success(contentFacade.getContentDetailByTMDB(mediaType, contentId, source))
                );
            default:
                throw new IllegalArgumentException("Invalid data source: " + source);
        }

    }
}
