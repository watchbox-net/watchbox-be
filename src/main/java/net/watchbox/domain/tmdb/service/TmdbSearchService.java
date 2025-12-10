package net.watchbox.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.tmdb.response.search.TmdbSearchResponse;
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
     * Multi Get 요청
     * @query 검색어
     * @include_adult 성인 컨텐츠 포함 여부
     * @language 언어
     * @page 페이지 번호
     */
    public TmdbSearchResponse searchMulti(String query, Integer page) {
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
                .bodyToMono(TmdbSearchResponse.class) // 응답을 Mono로 변환
                .onErrorMap(error -> new CustomException(ErrorCode.TMDB_SEARCH_BAD_GATEWAY)) // 예외 처리
                .block(); // 블로킹 호출로 결과 반환
    }

    /**
     * Movie Get 요청
     * @query 검색어
     * @include_adult 성인 컨텐츠 포함 여부
     * @language 언어
     * @primary_release_year 출시 년도
     * @page 페이지 번호
     * @region 지역 코드
     * @year 검색 년도
     */
    public TmdbSearchResponse searchMovie(String query, Integer page) {
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
                .bodyToMono(TmdbSearchResponse.class)
//                .map(this::convertToSearchListResponse)
                .onErrorMap(error -> new CustomException(ErrorCode.TMDB_SEARCH_BAD_GATEWAY)) // 예외 처리
                .block(); // 블로킹 호출로 결과 반환
    }

    /**
     * TV Get 요청
     * @query 검색어
     * @first_air_date_year 출시 년도
     * @include_adult 성인 컨텐츠 포함 여부
     * @language 언어
     * @page 페이지 번호
     * @year 검색 년도
     */
    public TmdbSearchResponse searchTv(String query, Integer page) {
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
                .bodyToMono(TmdbSearchResponse.class)
                .onErrorMap(error -> new CustomException(ErrorCode.TMDB_SEARCH_BAD_GATEWAY)) // 예외 처리
                .block(); // 블로킹 호출로 결과 반환
    }

    /**
     * Person Get 요청
     * @query 검색어
     * @include_adult 성인 컨텐츠 포함 여부
     * @language 언어
     * @page 페이지 번호
     */
    public TmdbSearchResponse searchPerson(String query, Integer page) {
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
                .bodyToMono(TmdbSearchResponse.class)
                .onErrorMap(error -> new CustomException(ErrorCode.TMDB_SEARCH_BAD_GATEWAY)) // 예외 처리
                .block(); // 블로킹 호출로 결과 반환
    }


}
