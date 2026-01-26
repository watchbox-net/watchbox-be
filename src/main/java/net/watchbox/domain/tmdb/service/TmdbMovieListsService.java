package net.watchbox.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.tmdb.client.TmdbClient;
import net.watchbox.domain.tmdb.response.movielists.TmdbMovieListsResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TmdbMovieListsService {
    private final TmdbClient tmdbClient;

    /**
     * Popular List Get 요청
     */
    public TmdbMovieListsResponse getPopularMovieLists(Integer page, String region) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/movie/popular")
                        .queryParam("page", page)
                        .queryParam("region", region) // 특정 국가의 인기 컨텐츠를 필터링 (KR, US, JP..)
                        .build())
                .retrieve()
                .bodyToMono(TmdbMovieListsResponse.class)
                .block();
    }
}
