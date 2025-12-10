package net.watchbox.global.dev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.search.dto.response.list.MultiSearchResponse;
import net.watchbox.domain.tmdb.response.search.TmdbSearchResultItem;
import net.watchbox.domain.tmdb.response.search.TmdbSearchResponse;
import net.watchbox.global.dev.dto.DevMultiSearchRequest;
import net.watchbox.global.dev.dto.DevMultiSearchResponseDto;
import net.watchbox.global.dev.dto.DevTmdbContentInfoDto;
import net.watchbox.global.properties.TmdbProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DevSearchService {
    private final WebClient.Builder webClientBuilder;
    private final TmdbProperties tmdbProperties;

    /**
     * 통합 검색 (영화 + TV + 배우)
     */
    public Mono<DevMultiSearchResponseDto> searchAll(DevMultiSearchRequest request) {
        String searchType = request.getType() != null ? request.getType() : "multi";

        switch (searchType.toLowerCase()) {
            case "movie":
                return searchMovies(request);
            case "tv":
                return searchTvShows(request);
            case "multi":
            default:
                return searchMulti(request);
        }
    }

    /**
     * 영화 검색
     */
    private Mono<DevMultiSearchResponseDto> searchMovies(DevMultiSearchRequest request) {
        WebClient webClient = webClientBuilder.baseUrl(tmdbProperties.getApi().getBaseUrl()).build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/movie")
                        .queryParam("api_key", tmdbProperties.getApi().getKey())
                        .queryParam("query", request.getQuery())
                        .queryParam("language", request.getLanguage())
                        .queryParam("page", request.getPage())
                        .queryParam("include_adult", false)
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponse.class)
                .map(response -> convertToMovieSearchResponse(response, "movie"))
                .doOnError(error -> log.error("영화 검색 실패: {}", error.getMessage()))
                .onErrorReturn(createEmptyResponse());
    }

    /**
     * TV 프로그램 검색
     */
    private Mono<DevMultiSearchResponseDto> searchTvShows(DevMultiSearchRequest request) {
        WebClient webClient = webClientBuilder.baseUrl(tmdbProperties.getApi().getBaseUrl()).build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/tv")
                        .queryParam("api_key", tmdbProperties.getApi().getKey())
                        .queryParam("query", request.getQuery())
                        .queryParam("language", request.getLanguage())
                        .queryParam("page", request.getPage())
                        .queryParam("include_adult", false)
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponse.class)
                .map(response -> convertToMovieSearchResponse(response, "tv"))
                .doOnError(error -> log.error("TV 프로그램 검색 실패: {}", error.getMessage()))
                .onErrorReturn(createEmptyResponse());
    }

    /**
     * 멀티 검색 (영화 + TV + 배우 통합)
     */
    private Mono<DevMultiSearchResponseDto> searchMulti(DevMultiSearchRequest request) {
        WebClient webClient = webClientBuilder.baseUrl(tmdbProperties.getApi().getBaseUrl()).build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/multi")
                        .queryParam("api_key", tmdbProperties.getApi().getKey())
                        .queryParam("query", request.getQuery())
                        .queryParam("language", request.getLanguage())
                        .queryParam("page", request.getPage())
                        .queryParam("include_adult", false)
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponse.class)
                .map(response -> convertToMovieSearchResponse(response, null))
                .doOnError(error -> log.error("통합 검색 실패: {}", error.getMessage()))
                .onErrorReturn(createEmptyResponse());
    }

    // //

    /**
     * 인기 영화 목록
     */
    public Mono<DevMultiSearchResponseDto> getPopularMovies(String language, int page) {
        WebClient webClient = webClientBuilder.baseUrl(tmdbProperties.getApi().getBaseUrl()).build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/popular")
                        .queryParam("api_key", tmdbProperties.getApi().getKey())
                        .queryParam("language", language)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponse.class)
                .map(response -> convertToMovieSearchResponse(response, "movie"))
                .doOnError(error -> log.error("인기 영화 조회 실패: {}", error.getMessage()))
                .onErrorReturn(createEmptyResponse());
    }

    /**
     * 현재 상영중인 영화
     */
    public Mono<DevMultiSearchResponseDto> getNowPlayingMovies(String language, int page) {
        WebClient webClient = webClientBuilder.baseUrl(tmdbProperties.getApi().getBaseUrl()).build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/now_playing")
                        .queryParam("api_key", tmdbProperties.getApi().getKey())
                        .queryParam("language", language)
                        .queryParam("page", page)
                        .queryParam("region", "KR")
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponse.class)
                .map(response -> convertToMovieSearchResponse(response, "movie"))
                .doOnError(error -> log.error("현재 상영 영화 조회 실패: {}", error.getMessage()))
                .onErrorReturn(createEmptyResponse());
    }

    /**
     * TMDB 응답을 프론트용 DTO로 변환
     */
    private DevMultiSearchResponseDto convertToMovieSearchResponse(TmdbSearchResponse tmdbResponse, String mediaType) {
        List<DevTmdbContentInfoDto> devTmdbContentInfoDtoList = tmdbResponse.getResults().stream()
                .filter(result -> mediaType == null ||
                        mediaType.equals(result.getMediaType()) ||
                        result.getMediaType() == null)
                .map(this::convertToMovieInfo)
                .collect(Collectors.toList());

        return new DevMultiSearchResponseDto(
                tmdbResponse.getPage(),
                tmdbResponse.getTotalResults(),
                tmdbResponse.getTotalPages(),
                devTmdbContentInfoDtoList
        );
    }

    /**
     * TMDB 결과를 MediaInfo로 변환
     */
    private DevTmdbContentInfoDto convertToMovieInfo(TmdbSearchResultItem result) {
        String title = result.getTitle() != null ? result.getTitle() : result.getName();
        String originalTitle = result.getOriginalTitle() != null ?
                result.getOriginalTitle() : result.getOriginalName();
        String releaseDate = result.getReleaseDate() != null ?
                result.getReleaseDate() : result.getFirstAirDate();
        String mediaType = result.getMediaType() != null ?
                result.getMediaType() : "movie";

        return new DevTmdbContentInfoDto(
                result.getId(),
                title,
                originalTitle,
                result.getOverview(),
                result.getGenreIds(),
                buildImageUrl(result.getPosterPath()),
                buildImageUrl(result.getBackdropPath()),
                releaseDate,
                result.getVoteAverage(),
                result.getVoteCount(),
                result.getPopularity(),
                mediaType,
                result.getOriginalLanguage()
        );
    }

    /**
     * 이미지 전체 URL 생성
     */
    private String buildImageUrl(String path) {
        return path != null ? tmdbProperties.getImageUrl() + path : null;
    }

    /**
     * 빈 응답 생성
     */
    private DevMultiSearchResponseDto createEmptyResponse() {
        return new DevMultiSearchResponseDto(1, 0, 0, Collections.emptyList());
    }

    public List<MultiSearchResponse> multiSearch(String query) {
        return null;
    }
}
