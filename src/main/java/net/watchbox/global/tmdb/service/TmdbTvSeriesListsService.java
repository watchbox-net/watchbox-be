package net.watchbox.global.tmdb.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import net.watchbox.global.tmdb.client.TmdbClient;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Observed
public class TmdbTvSeriesListsService { // TV SERIES LISTS
    private final TmdbClient tmdbClient;

    /**
     * Popular List
     */
    public TmdbTvSeriesListsResponse getPopularTvSeriesLists(Integer page) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/tv/popular")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(TmdbTvSeriesListsResponse.class)
                .block();
    }

    /**
     * Top Rated
     */
    public TmdbTvSeriesListsResponse getTopRatedTvSeriesLists(Integer page) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/tv/top_rated")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(TmdbTvSeriesListsResponse.class)
                .block();
    }

    /**
     * On The Air
     */
    public TmdbTvSeriesListsResponse getOnTheAirTvSeriesLists(Integer page) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/tv/on_the_air")
                        .queryParam("page", page)
                        .queryParam("timezone", "Asia/Seoul") // 특정 시간대의 방영중인 TV 시리즈를 필터링 (예: Asia/Seoul, America/New_York)
                        .build())
                .retrieve()
                .bodyToMono(TmdbTvSeriesListsResponse.class)
                .block();
    }
}
