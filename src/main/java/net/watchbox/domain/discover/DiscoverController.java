package net.watchbox.domain.discover;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.base.dto.list.ContentPageResponse;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/discover")
@Tag(name = "Discover", description = "영화, 시리즈 트렌드 리스트 조회 API")
public class DiscoverController {
    private final DiscoverFacade discoverFacade;

    @GetMapping("/popular/movies")
    public ResponseEntity<ApiResponse<ContentPageResponse>> getPopularMovies(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "KR") String region) {
        return ResponseEntity.ok(
                ApiResponse.success(discoverFacade.getPopularMovies(page, region))
        );
    }

    @GetMapping("/popular/tv")
    public ResponseEntity<ApiResponse<ContentPageResponse>> getPopularTvSeries(
            @RequestParam(defaultValue = "1") Integer page) {
        return ResponseEntity.ok(
                ApiResponse.success(discoverFacade.getPopularTvSeries(page))
        );
    }

}
