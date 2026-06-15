package net.watchbox.domain.discover.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.dto.list.ContentPageResponse;
import net.watchbox.domain.discover.facade.DiscoverFacade;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.global.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/discover")
@Tag(name = "Discover", description = "영화, 시리즈 트렌드 리스트 조회 API")
@Observed
public class DiscoverController {
    private final DiscoverFacade discoverFacade;

//    @GetMapping("/home")


    @Operation(summary = "전세계 인기 영화")
    @GetMapping("/popular/movies")
    public ResponseEntity<ApiResponse<ContentPageResponse>> getPopularMovies(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "false") boolean withRecord,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(discoverFacade.getPopularMovies(page, withRecord, member))
        );
    }

    @GetMapping("/popular/tv")
    public ResponseEntity<ApiResponse<ContentPageResponse>> getPopularTvSeries(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "false") boolean withRecord,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(discoverFacade.getPopularTvSeries(page, withRecord, member))
        );
    }

    @GetMapping("/top-rated/movies")
    public ResponseEntity<ApiResponse<ContentPageResponse>> getTopRatedMovies(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "false") boolean withRecord,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(discoverFacade.getTopRatedMovies(page, withRecord, member))
        );
    }

    @GetMapping("/top-rated/tv")
    public ResponseEntity<ApiResponse<ContentPageResponse>> getTopRatedTvSeries(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "false") boolean withRecord,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(discoverFacade.getTopRatedTvSeries(page, withRecord, member))
        );
    }

    @GetMapping("/now-showing/movies")
    public ResponseEntity<ApiResponse<ContentPageResponse>> getNowShowingMovies(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "false") boolean withRecord,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(discoverFacade.getNowPlayingMovies(page, withRecord, member))
        );
    }

    @GetMapping("/now-showing/tv")
    public ResponseEntity<ApiResponse<ContentPageResponse>> getNowShowingTvSeries(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "false") boolean withRecord,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(discoverFacade.getOnTheAirTvSeries(page, withRecord, member))
        );
    }

    @GetMapping("/trending/movies") // ToDo: 일단 확인만 해보고 나중에 분기 나누기
    public ResponseEntity<ApiResponse<ContentPageResponse>> getTrendingMovies(
            @RequestParam(defaultValue = "week") String timeWindow,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "false") boolean withRecord,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(discoverFacade.getTrendingMovies(timeWindow, page, withRecord, member))
        );
    }

    @GetMapping("/trending/tv")
    public ResponseEntity<ApiResponse<ContentPageResponse>> getTrendingTv(
            @RequestParam(defaultValue = "week") String timeWindow,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "false") boolean withRecord,
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(discoverFacade.getTrendingTv(timeWindow, page, withRecord, member))
        );
    }
}
