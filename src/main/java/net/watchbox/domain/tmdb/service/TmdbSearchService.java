package net.watchbox.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.tmdb.dto.search.TmdbSearchResponseDto;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import net.watchbox.global.properties.TmdbProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmdbSearchService {
    private final WebClient.Builder webClientBuilder;
    private final TmdbProperties tmdbProperties;

    /**
     * 통합 검색 (영화 + TV + 인물 통합)
     */
    public TmdbSearchResponseDto searchMulti(String query, Integer page) {
        WebClient webClient = webClientBuilder.baseUrl(tmdbProperties.getApi().getBaseUrl()).build();

        return webClient.get() // GET 요청 시작
                .uri(uriBuilder -> uriBuilder // URL 구성
                        .path("/search/multi")
                        .queryParam("api_key", tmdbProperties.getApi().getKey())
                        .queryParam("query",query)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .queryParam("include_adult", true)
                        .build())
                .retrieve() // 응답 받기
                .bodyToMono(TmdbSearchResponseDto.class) // 응답을 Mono로 변환
                .onErrorMap(error -> new CustomException(ErrorCode.TMDB_SEARCH_BAD_GATEWAY)) // 예외 처리
                .block(); // 블로킹 호출로 결과 반환
    }

    /**
     * 영화 검색
     */
    public TmdbSearchResponseDto searchMovie(String query, Integer page) {
        WebClient webClient = webClientBuilder.baseUrl(tmdbProperties.getApi().getBaseUrl()).build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/movie")
                        .queryParam("api_key", tmdbProperties.getApi().getKey())
                        .queryParam("query",query)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .queryParam("include_adult", true)
                        // primary_release_year, region, year 등 추가 가능
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponseDto.class)
//                .map(this::convertToSearchListResponse)
                .onErrorMap(error -> new CustomException(ErrorCode.TMDB_SEARCH_BAD_GATEWAY)) // 예외 처리
                .block(); // 블로킹 호출로 결과 반환
    }

    /**
     * TV 프로그램 검색
     */
    public TmdbSearchResponseDto searchTv(String query, Integer page) {
        WebClient webClient = webClientBuilder.baseUrl(tmdbProperties.getApi().getBaseUrl()).build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/tv")
                        .queryParam("api_key", tmdbProperties.getApi().getKey())
                        .queryParam("query", query)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .queryParam("include_adult", true)
                        // first_air_date_year, year 등 추가 가능
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponseDto.class)
                .onErrorMap(error -> new CustomException(ErrorCode.TMDB_SEARCH_BAD_GATEWAY)) // 예외 처리
                .block(); // 블로킹 호출로 결과 반환
    }

    /**
     * 인물 검색
     */
    public TmdbSearchResponseDto searchPerson(String query, Integer page) {
        WebClient webClient = webClientBuilder.baseUrl(tmdbProperties.getApi().getBaseUrl()).build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/person")
                        .queryParam("api_key", tmdbProperties.getApi().getKey())
                        .queryParam("query", query)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .queryParam("include_adult", true)
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponseDto.class)
                .onErrorMap(error -> new CustomException(ErrorCode.TMDB_SEARCH_BAD_GATEWAY)) // 예외 처리
                .block(); // 블로킹 호출로 결과 반환
    }


}
