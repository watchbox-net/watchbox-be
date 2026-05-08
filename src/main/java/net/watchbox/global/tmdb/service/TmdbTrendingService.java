package net.watchbox.global.tmdb.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import net.watchbox.global.tmdb.client.TmdbClient;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Observed
public class TmdbTrendingService { // TRENDING
    private final TmdbClient tmdbClient;

    /**
     * Movies
     * @time_window (day, week)
     */
    public TmdbMovieListsResponse getTrendingMovies(String timeWindow, Integer page) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/trending/movie/{timeWindow}")
                        .queryParam("page", page)
                        .build(timeWindow))
                .retrieve()
                .bodyToMono(TmdbMovieListsResponse.class)
                .block();
    }

    /**
     * TV
     * @time_window (day, week)
     */
    public TmdbTvSeriesListsResponse getTrendingTv(String timeWindow, Integer page) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/trending/tv/{timeWindow}")
                        .queryParam("page", page)
                        .build(timeWindow))
                .retrieve()
                .bodyToMono(TmdbTvSeriesListsResponse.class)
                .block();
    }
}
