package net.watchbox.global.dev.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.content.dto.list.ContentSummary;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.mapper.ContentSummaryMapper;
import net.watchbox.domain.content.service.ContentCommandService;
import net.watchbox.domain.content.service.ContentQueryService;
import net.watchbox.domain.content.sub.movie.entity.Movie;
import net.watchbox.domain.content.sub.person.entity.Person;
import net.watchbox.domain.content.sub.tv.entity.Tv;
import net.watchbox.global.tmdb.response.movies.TmdbMoviesDetailsResponse;
import net.watchbox.global.tmdb.response.people.TmdbPeopleDetailsResponse;
import net.watchbox.global.tmdb.response.tvseries.TmdbTvSeriesDetailsResponse;
import net.watchbox.global.tmdb.service.TmdbMoviesService;
import net.watchbox.global.tmdb.service.TmdbPeopleService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/contents")
@Tag(name = "DevContent")
@Hidden
public class DevContentSaveController {
    private final ContentCommandService contentCommandService;
    private final ContentQueryService contentQueryService;
    private final TmdbMoviesService tmdbMoviesService;
    private final TmdbTvSeriesService tmdbTvSeriesService;
    private final TmdbPeopleService tmdbPeopleService;

    /**
     * MOVIES Details Get 및 저장, 메인 응답으로 반환
     * 49797
     */
    @PostMapping("/movie/detail/{tmdbId}")
    public ResponseEntity<ContentSummary> fetchAndSaveTmdbMovieDetails(
            @PathVariable Long tmdbId
    ) {
        Optional<Content> foundContent = contentQueryService.findContentById(tmdbId);
//        Content content = foundContent.orElseGet(() -> contentCommandService.createContentByTmdbId(tmdbId));
        Content content;
        TmdbMoviesDetailsResponse response = tmdbMoviesService.getMovieDetails(tmdbId);
        if (foundContent.isPresent()) {
            System.out.println("이미 Content가 존재합니다. tmdbId = " + tmdbId);
        } else {
            // 1) Content 정보 저장 - SQL Insert
            content = contentCommandService.saveContent(tmdbId, MediaType.MOVIE);
            // 2) Content의 하위 엔티티 저장
            // TMDB API 상세 검색으로 TmdbMovieDetailsResponse 호출
            // TmdbMovieDetailsResponse 가공하여 Movie 저장
            contentCommandService.saveMovieContent(content, response);
        }
//        movieRepository.flush();
        Movie movie = contentQueryService.getMovieByIdOrThrow(tmdbId);
        return ResponseEntity.ok(ContentSummaryMapper.fromMovie(movie));
    }

    /**
     * TV SERIES Details Get 및 저장, 메인 응답으로 반환
     * 93405
     */
    @PostMapping("/tv/detail/{tmdbId}")
    public ResponseEntity<ContentSummary> fetchAndSaveTmdbTvDetails(
            @PathVariable Long tmdbId
    ) {
        Optional<Content> foundContent = contentQueryService.findContentById(tmdbId);
        Content content;
        TmdbTvSeriesDetailsResponse response = tmdbTvSeriesService.getTvSeriesDetails(tmdbId);
        if (foundContent.isPresent()) {
            content = foundContent.get();
            System.out.println("이미 Content가 존재합니다. tmdbId = " + tmdbId);
        }else{
            // 1) Content 정보 저장
            content = contentCommandService.saveContent(tmdbId, MediaType.TV);
            // 2) Content의 하위 엔티티 저장
            contentCommandService.saveTvContent(content, response);
        }

        Tv tv = contentQueryService.getTvByIdOrThrow(tmdbId);
        return ResponseEntity.ok(ContentSummaryMapper.fromTv(tv));
    }


    /**
     * PEOPLE Details Get 및 저장, 메인 응답으로 반환
     *
     */
    @PostMapping("/person/detail/{tmdbId}")
    public ResponseEntity<ContentSummary> fetchAndSaveTmdbPersonDetails(
            @PathVariable Long tmdbId
    ) {
        Optional<Content> foundContent = contentQueryService.findContentById(tmdbId);
        Content content;
        TmdbPeopleDetailsResponse response = tmdbPeopleService.getPeopleDetails(tmdbId);
        if (foundContent.isPresent()) {
            System.out.println("이미 Content가 존재합니다. tmdbId = " + tmdbId);
        }else{
            // 1) Content 정보 저장
            content = contentCommandService.saveContent(tmdbId, MediaType.PERSON);
            // 2) Content의 하위 엔티티 저장
            contentCommandService.savePersonContent(content, response);
        }

        Person person = contentQueryService.getPersonByIdOrThrow(tmdbId);
        return ResponseEntity.ok(ContentSummaryMapper.fromPerson(person));
    }

    
    /**
     * Content 및 관련 하위 엔티티들 전부 삭제
     */
    @DeleteMapping("/{tmdbId}")
    public ResponseEntity<Void> deleteContentCascade(
            @PathVariable Long tmdbId
    ) {
        contentCommandService.deleteContentCascade(tmdbId);
        return ResponseEntity.noContent().build();
    }
}
