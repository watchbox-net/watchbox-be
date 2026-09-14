package net.watchbox.global.dev.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.global.tmdb.response.movies.TmdbMoviesDetailsResponse;
import net.watchbox.global.tmdb.response.people.TmdbPersonDetailsResponse;
import net.watchbox.global.tmdb.response.tvseries.TmdbTvSeriesDetailsResponse;
import net.watchbox.global.tmdb.service.TmdbMoviesService;
import net.watchbox.global.tmdb.service.TmdbPeopleService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/tmdb")
@Tag(name = "9-2 [Dev] TMDB")
@Hidden
public class DevTmdbDetailsController {
    private final TmdbMoviesService tmdbMoviesService;
    private final TmdbTvSeriesService tmdbTvSeriesService;
    private final TmdbPeopleService tmdbPeopleService;

    /**
     * MOVIES Details Get 요청
     * 49797
     */
    @GetMapping("/movies/details/{tmdbId}")
    public ResponseEntity<TmdbMoviesDetailsResponse> fetchTmdbMovieDetails(
            @PathVariable Long tmdbId
    ) {
        return ResponseEntity.ok(tmdbMoviesService.getMovieDetailsWithCVP(tmdbId));
    }

    /**
     * TV SERIES Details Get 요청
     * 93405
     */
    @GetMapping("/tvseries/details/{tmdbId}")
    public ResponseEntity<TmdbTvSeriesDetailsResponse> fetchTmdbTvSeriesDetails(
            @PathVariable Long tmdbId
    ) {
        return ResponseEntity.ok(tmdbTvSeriesService.getTvSeriesDetails(tmdbId));
    }

    /**
     * PEOPLE Details Get 요청
     * 150242
     */
    @GetMapping("/people/details/{tmdbId}")
    public ResponseEntity<TmdbPersonDetailsResponse> fetchTmdbPeopleDetails(
            @PathVariable Long tmdbId
    ) {
        return ResponseEntity.ok(tmdbPeopleService.getPeopleDetailsWithCI(tmdbId));
    }

    //


}
