package net.watchbox.global.dev.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.content.common.entity.MediaType;
import net.watchbox.domain.content.common.service.ContentCommandService;
import net.watchbox.domain.content.common.service.ContentQueryService;
import net.watchbox.domain.content.movie.dto.response.MovieResponse;
import net.watchbox.domain.content.movie.entity.Movie;
import net.watchbox.domain.content.movie.repository.MovieRepository;
import net.watchbox.domain.tmdb.response.movies.TmdbMovieDetailsResponse;
import net.watchbox.domain.tmdb.response.people.TmdbPeopleDetailsResponse;
import net.watchbox.domain.tmdb.response.tvseries.TmdbTvSeriesDetailsResponse;
import net.watchbox.domain.tmdb.service.TmdbMoviesService;
import net.watchbox.domain.tmdb.service.TmdbPeopleService;
import net.watchbox.domain.tmdb.service.TmdbTvSeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/tmdb")
@Slf4j
public class DevTmdbDetailsController {
    private final TmdbMoviesService tmdbMoviesService;
    private final TmdbTvSeriesService tmdbTvSeriesService;
    private final TmdbPeopleService tmdbPeopleService;
    private final ContentCommandService contentCommandService;
    private final ContentQueryService contentQueryService;

    private final MovieRepository movieRepository;

    /**
     * MOVIES Details Get 요청
     * 49797
     */
    @GetMapping("/movies/details/{tmdbId}")
    public ResponseEntity<TmdbMovieDetailsResponse> fetchTmdbMovieDetails(
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

    /**
     * MOVIES Details Get 및 저장, 리스트 응답으로 반환
     * 49797
     */
    @GetMapping("/movies/details/{tmdbId}/save")
    public ResponseEntity<MovieResponse> fetchAndSaveTmdbMovieDetails(
            @PathVariable Long tmdbId
    ) {
        Optional<Content> foundContent = contentQueryService.findContentById(tmdbId);
//        Content content = foundContent.orElseGet(() -> contentCommandService.createContentByTmdbId(tmdbId));
        Content content;
        TmdbMovieDetailsResponse response = tmdbMoviesService.getMovieDetails(tmdbId);
        if (foundContent.isPresent()) {
            content = foundContent.get();
            System.out.println("이미 Content가 존재합니다. tmdbId = " + tmdbId);
        } else {
            // 1) Content 정보 저장 - SQL Insert
            content = contentCommandService.createContent(tmdbId, MediaType.MOVIE);
            // 2) Content의 하위 엔티티 저장
            // TMDB API 상세 검색으로 TmdbMovieDetailsResponse 호출
            // TmdbMovieDetailsResponse 가공하여 Movie 저장
            contentCommandService.saveMovieContent(content, response);
        }
//        movieRepository.flush();
        Movie movie = contentQueryService.getMovieByIdOrThrow(tmdbId);
        return ResponseEntity.ok(MovieResponse.from(movie));
    }
}
