package net.watchbox.global.tmdb.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import net.watchbox.global.tmdb.client.TmdbClient;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Observed
public class TmdbMovieListsService { // MOVIE LISTS
    private final TmdbClient tmdbClient;

    /**
     * Popular
     */
    public TmdbMovieListsResponse getPopularMovieLists(Integer page) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/movie/popular")
                        .queryParam("page", page)
//                        .queryParam("region", "KR") // 특정 국가의 인기 컨텐츠를 필터링 (KR, US, JP..)
                        .build())
                .retrieve()
                .bodyToMono(TmdbMovieListsResponse.class)
                .block();
    }

    /**
     * Top Rated
     */
    public TmdbMovieListsResponse getTopRatedMovieLists(Integer page) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/movie/top_rated")
                        .queryParam("page", page)
//                        .queryParam("region", "KR") // 특정 국가의 인기 컨텐츠를 필터링 (KR, US, JP..)
                        .build())
                .retrieve()
                .bodyToMono(TmdbMovieListsResponse.class)
                .block();
    }

    /**
     * Now Playing
     */
    public TmdbMovieListsResponse getNowPlayingMovieLists(Integer page) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/movie/now_playing")
                        .queryParam("page", page)
                        .queryParam("region", "KR") // 특정 국가의 인기 컨텐츠를 필터링 (KR, US, JP..)
                        .build())
                .retrieve()
                .bodyToMono(TmdbMovieListsResponse.class)
                .block();
    }
}
