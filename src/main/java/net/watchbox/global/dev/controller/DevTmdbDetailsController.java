package net.watchbox.global.dev.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.tmdb.response.movies.TmdbMoviesDetailsResponse;
import net.watchbox.domain.tmdb.response.people.TmdbPeopleDetailsResponse;
import net.watchbox.domain.tmdb.response.tvseries.TmdbTvSeriesDetailsResponse;
import net.watchbox.domain.tmdb.service.TmdbMoviesService;
import net.watchbox.domain.tmdb.service.TmdbPeopleService;
import net.watchbox.domain.tmdb.service.TmdbTvSeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/tmdb")
@Slf4j
@Tag(name = "DevTmdbDetails")
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
        return ResponseEntity.ok(tmdbMoviesService.getMovieDetails(tmdbId));
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
    public ResponseEntity<TmdbPeopleDetailsResponse> fetchTmdbPeopleDetails(
            @PathVariable Long tmdbId
    ) {
        return ResponseEntity.ok(tmdbPeopleService.getPeopleDetails(tmdbId));
    }

    //


}
