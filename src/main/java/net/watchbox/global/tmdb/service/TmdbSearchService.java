package net.watchbox.global.tmdb.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.global.tmdb.client.TmdbClient;
import net.watchbox.global.tmdb.response.search.TmdbSearchCommonResponse;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class TmdbSearchService {
    private final TmdbClient tmdbClient;

    /**
     * Multi
     * https://api.themoviedb.org/3/search/multi
     * 검색 페이지에 필요한 정보 요청
     */
    public TmdbSearchCommonResponse searchMulti(String query, Integer page) {
        return tmdbClient.baseWebClient()
                .get() // GET 요청 시작
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/search/multi")
                        .queryParam("query",query)
                        .queryParam("page", page)
                        .queryParam("include_adult", true)
                        .build())
                .retrieve() // 응답 받기
                .bodyToMono(TmdbSearchCommonResponse.class) // 응답을 Mono로 변환
                .onErrorMap(error -> new CustomException(ErrorCode.TMDB_SEARCH_BAD_GATEWAY)) // 예외 처리
                .block();
    }

    /**
     * Movie
     * https://api.themoviedb.org/3/search/movie
     * 검색 페이지에 필요한 정보 요청
     */
    public TmdbSearchCommonResponse searchMovie(String query, Integer page) {

        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/search/movie")
                        .queryParam("query",query)
                        .queryParam("page", page)
                        .queryParam("include_adult", true)
                        // primary_release_year, region, year 등 추가 가능
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchCommonResponse.class)
//                .map(this::convertToSearchListResponse)
                .onErrorMap(error -> new CustomException(ErrorCode.TMDB_SEARCH_BAD_GATEWAY)) // 예외 처리
                .block();
    }

    /**
     * TV
     * https://api.themoviedb.org/3/search/tv
     * 검색 페이지에 필요한 정보 요청
     */
    public TmdbSearchCommonResponse searchTv(String query, Integer page) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/search/tv")
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .queryParam("include_adult", true)
                        // first_air_date_year, year 등 추가 가능
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchCommonResponse.class)
                .onErrorMap(error -> new CustomException(ErrorCode.TMDB_SEARCH_BAD_GATEWAY)) // 예외 처리
                .block();
    }

    /**
     * Person
     * https://api.themoviedb.org/3/search/person
     * 검색 페이지에 필요한 정보 요청
     */
    public TmdbSearchCommonResponse searchPerson(String query, Integer page) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/search/person")
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .queryParam("include_adult", true)
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchCommonResponse.class)
                .onErrorMap(error -> new CustomException(ErrorCode.TMDB_SEARCH_BAD_GATEWAY)) // 예외 처리
                .block();
    }

    /**
     * Person
     * https://api.themoviedb.org/3/search/person
     * 인물의 영문 이름과 원래 이름을 알기 위한 요청
     */
    public TmdbSearchCommonResponse searchPersonExtraNames(String nameKo) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder, "en-US")
                        .path("/search/person")
                        .queryParam("query", nameKo)
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchCommonResponse.class)
                .onErrorMap(error -> new CustomException(ErrorCode.TMDB_SEARCH_BAD_GATEWAY)) // 예외 처리
                .block();
    }

}
