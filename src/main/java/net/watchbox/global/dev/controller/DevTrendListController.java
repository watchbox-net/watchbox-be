package net.watchbox.global.dev.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import net.watchbox.global.tmdb.service.TmdbMovieListsService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesListsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/dev/tmdb")
@Tag(name = "9-2 [Dev] TMDB")
public class DevTrendListController { // 인기, 평점 높은 x 영화, TV 리스트 조회
    private final TmdbMovieListsService tmdbMovieListsService;
    private final TmdbTvSeriesListsService tmdbTvSeriesListsService;

    // 인기 영화 리스트 조회
    @GetMapping("/popular/movies")
    public TmdbMovieListsResponse getPopularMovieList() {
        return tmdbMovieListsService.getPopularMovieLists(1);
    }

    // 인기 TV 시리즈 리스트 조회
    @GetMapping("/popular/tvs")
    public TmdbTvSeriesListsResponse getPopularTvSeriesList() {
        return tmdbTvSeriesListsService.getPopularTvSeriesLists(1);
    }
}
