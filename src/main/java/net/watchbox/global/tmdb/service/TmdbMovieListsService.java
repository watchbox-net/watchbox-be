package net.watchbox.global.tmdb.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.global.tmdb.cache.TmdbResponseCache;
import net.watchbox.global.tmdb.client.TmdbClient;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * TMDB MOVIE LISTS.
 *
 * <p>메서드는 쌍으로 제공한다.
 * <ul>
 *   <li>{@code xxx(...)} — blocking. 단건 조회(개별 discover 엔드포인트)에서 사용.</li>
 *   <li>{@code xxxMono(...)} — 논블로킹. 홈 BFF 처럼 여러 리스트를 {@code Mono.zip} 으로
 *       <b>병렬</b>로 받을 때 사용. 순차로 {@code block()} 을 반복하면 응답시간이 합산되므로
 *       조합 호출에서는 반드시 Mono 버전을 써야 한다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class TmdbMovieListsService { // MOVIE LISTS
    private final TmdbClient tmdbClient;
    private final TmdbResponseCache cache;

    /**
     * Popular
     */
    public TmdbMovieListsResponse getPopularMovieLists(Integer page) {
        return getPopularMovieListsMono(page).block();
    }

    public Mono<TmdbMovieListsResponse> getPopularMovieListsMono(Integer page) {
        return cache.readThrough("tmdb:movie:popular:" + page,
                TmdbResponseCache.Ttl.STABLE, TmdbMovieListsResponse.class,
                () -> tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/movie/popular")
                        .queryParam("page", page)
//                        .queryParam("region", "KR") // 특정 국가의 인기 콘텐츠를 필터링 (KR, US, JP..)
                        .build())
                .retrieve()
                .bodyToMono(TmdbMovieListsResponse.class));
    }

    /**
     * Top Rated
     */
    public TmdbMovieListsResponse getTopRatedMovieLists(Integer page) {
        return getTopRatedMovieListsMono(page).block();
    }

    public Mono<TmdbMovieListsResponse> getTopRatedMovieListsMono(Integer page) {
        return cache.readThrough("tmdb:movie:top-rated:" + page,
                TmdbResponseCache.Ttl.STABLE, TmdbMovieListsResponse.class,
                () -> tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/movie/top_rated")
                        .queryParam("page", page)
//                        .queryParam("region", "KR") // 특정 국가의 인기 콘텐츠를 필터링 (KR, US, JP..)
                        .build())
                .retrieve()
                .bodyToMono(TmdbMovieListsResponse.class));
    }

    /**
     * Now Playing
     */
    public TmdbMovieListsResponse getNowPlayingMovieLists(Integer page) {
        return getNowPlayingMovieListsMono(page).block();
    }

    public Mono<TmdbMovieListsResponse> getNowPlayingMovieListsMono(Integer page) {
        return cache.readThrough("tmdb:movie:now-playing:" + page,
                TmdbResponseCache.Ttl.NOW_SHOWING, TmdbMovieListsResponse.class,
                () -> tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/movie/now_playing")
                        .queryParam("page", page)
                        .queryParam("region", "KR") // 특정 국가의 인기 콘텐츠를 필터링 (KR, US, JP..)
                        .build())
                .retrieve()
                .bodyToMono(TmdbMovieListsResponse.class));
    }
}
