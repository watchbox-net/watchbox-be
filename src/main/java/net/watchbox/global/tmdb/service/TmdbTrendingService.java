package net.watchbox.global.tmdb.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import net.watchbox.global.tmdb.cache.TmdbResponseCache;
import net.watchbox.global.tmdb.client.TmdbClient;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * TMDB TRENDING.
 *
 * <p>blocking / Mono 쌍 제공 이유는 {@link TmdbMovieListsService} 주석 참고.
 */
@Service
@RequiredArgsConstructor
@Observed
public class TmdbTrendingService { // TRENDING
    private final TmdbClient tmdbClient;
    private final TmdbResponseCache cache;

    /**
     * Movies
     * @time_window (day, week)
     */
    public TmdbMovieListsResponse getTrendingMovies(String timeWindow, Integer page) {
        return getTrendingMoviesMono(timeWindow, page).block();
    }

    public Mono<TmdbMovieListsResponse> getTrendingMoviesMono(String timeWindow, Integer page) {
        return cache.readThrough("tmdb:trending:movie:" + timeWindow + ":" + page,
                TmdbResponseCache.Ttl.TRENDING, TmdbMovieListsResponse.class,
                () -> tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/trending/movie/{timeWindow}")
                        .queryParam("page", page)
                        .build(timeWindow))
                .retrieve()
                .bodyToMono(TmdbMovieListsResponse.class));
    }

    /**
     * TV
     * @time_window (day, week)
     */
    public TmdbTvSeriesListsResponse getTrendingTv(String timeWindow, Integer page) {
        return getTrendingTvMono(timeWindow, page).block();
    }

    public Mono<TmdbTvSeriesListsResponse> getTrendingTvMono(String timeWindow, Integer page) {
        return cache.readThrough("tmdb:trending:tv:" + timeWindow + ":" + page,
                TmdbResponseCache.Ttl.TRENDING, TmdbTvSeriesListsResponse.class,
                () -> tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/trending/tv/{timeWindow}")
                        .queryParam("page", page)
                        .build(timeWindow))
                .retrieve()
                .bodyToMono(TmdbTvSeriesListsResponse.class));
    }
}
