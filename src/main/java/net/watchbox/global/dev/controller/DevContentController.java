package net.watchbox.global.dev.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.content.common.entity.MediaType;
import net.watchbox.domain.content.common.service.ContentCommandService;
import net.watchbox.domain.content.common.service.ContentQueryService;
import net.watchbox.domain.content.movie.dto.response.MovieResponse;
import net.watchbox.domain.content.movie.entity.Movie;
import net.watchbox.domain.tmdb.response.movies.TmdbMovieDetailsResponse;
import net.watchbox.domain.tmdb.service.TmdbMoviesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/contents")
@Slf4j
public class DevContentController {
    private final ContentCommandService contentCommandService;
    private final ContentQueryService contentQueryService;
    private final TmdbMoviesService tmdbMoviesService;

    /**
     * MOVIES Details Get 및 저장, 리스트 응답으로 반환
     * 49797
     */
    @PostMapping("/movie/detail/{tmdbId}")
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

    /**
     * Content 및 관련 하위 엔티티들 전부 삭제
     */
    @DeleteMapping("/{tmdbId}")
    public ResponseEntity<Void> deleteContentMovieMovieDetail(
            @PathVariable Long tmdbId
    ) {
        contentCommandService.deleteContentCascade(tmdbId);
        return ResponseEntity.noContent().build();
    }
}
