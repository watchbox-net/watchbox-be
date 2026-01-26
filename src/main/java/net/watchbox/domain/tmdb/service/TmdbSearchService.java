package net.watchbox.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.tmdb.client.TmdbClient;
import net.watchbox.domain.tmdb.response.search.TmdbSearchCommonResponse;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmdbSearchService {
    private final TmdbClient tmdbClient;

    /**
     * Multi Get 요청
     * @query 검색어
     * @include_adult 성인 컨텐츠 포함 여부
     * @language 언어
     * @page 페이지 번호
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
                .block(); // 블로킹 호출로 결과 반환
    }

    /**
     * Person Get 요청
     * @query 검색어
     * @include_adult 성인 컨텐츠 포함 여부
     * @language 언어
     * @page 페이지 번호
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
                .block(); // 블로킹 호출로 결과 반환
    }

}
