package net.watchbox.domain.discover.warmup;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.mapper.tmdb.TmdbDiscoverDtoMapper;
import net.watchbox.global.tmdb.cache.TmdbResponseCache;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import net.watchbox.global.tmdb.service.TmdbMovieListsService;
import net.watchbox.global.tmdb.service.TmdbTrendingService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesListsService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 홈 화면 8개 섹션을 TMDB 에서 가져온다. 개인화 이전, <b>캐시 대상 계층</b>만 다룬다.
 *
 * <p>조회({@link #load})와 캐시 선제 갱신({@link #refreshCache})이 <b>같은 섹션 목록</b>을 쓰도록
 * 한 곳에 모았다. 목록이 어긋나면 워밍이 채운 키와 조회가 찾는 키가 달라져 캐시가 무용지물이 된다.
 *
 * <p><b>병렬 fan-out</b>: TMDB 8건을 {@link Mono#zip} 으로 동시에 호출한다. 한 메서드 안에서
 * blocking 메서드를 8번 부르면 응답시간이 8건의 <i>합</i>이 되어, 프론트가 8개 엔드포인트를
 * 병렬 호출하던 기존보다 오히려 느려진다. zip 은 가장 느린 1건에 수렴한다.
 *
 * <p><b>섹션별 실패 격리</b>: zip 은 하나만 실패해도 전체가 실패한다. 홈은 일부 섹션이 죽어도
 * 나머지는 보여야 하므로 섹션마다 에러를 빈 리스트로 흡수한다.
 * (프론트의 {@code Promise.allSettled} 가 하던 역할을 백엔드로 옮긴 것)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Observed
public class HomeTmdbSectionLoader {

    private final TmdbMovieListsService tmdbMovieListsService;
    private final TmdbTvSeriesListsService tmdbTvSeriesListsService;
    private final TmdbTrendingService tmdbTrendingService;

    /**
     * 영화·시리즈 섹션 묶음. 각 리스트의 순서는 <b>[trending, popular, nowShowing, topRated]</b> 로 고정이다.
     * 호출부가 인덱스로 꺼내 쓰므로 순서가 바뀌면 조용한 버그가 된다.
     */
    public record Sections(List<List<ContentItem>> movies, List<List<ContentItem>> tv) {
    }

    /** 8개 섹션을 병렬로 조회한다. 캐시가 있으면 캐시에서, 없으면 TMDB 에서 받아 채운다. */
    public Sections load(String timeWindow, Integer page) {
        Object[] results = Mono.zip(sectionMonos(timeWindow, page), sections -> sections).block();

        return new Sections(
                List.of(at(results, 0), at(results, 1), at(results, 2), at(results, 3)),
                List.of(at(results, 4), at(results, 5), at(results, 6), at(results, 7))
        );
    }

    /**
     * 캐시를 TTL 만료 <b>전에</b> 선제 갱신한다. 응답은 쓰지 않고 버린다.
     *
     * <p>{@link #load} 를 그대로 호출하면 캐시가 hit 이라 TMDB 를 부르지 않아 <b>갱신이 되지 않는다</b>.
     * 그래서 캐시 조회를 건너뛰도록 {@link TmdbResponseCache#forceRefresh()} 를 Context 에 심는다.
     * ({@code contextWrite} 는 상류에 적용되므로 8개 섹션 전체가 대상이 된다)
     */
    public void refreshCache(String timeWindow, Integer page) {
        Mono.zip(sectionMonos(timeWindow, page), sections -> sections)
                .contextWrite(TmdbResponseCache.forceRefresh())
                .block();
    }

    private List<Mono<List<ContentItem>>> sectionMonos(String timeWindow, Integer page) {
        return List.of(
                movieSection(tmdbTrendingService.getTrendingMoviesMono(timeWindow, page)),
                movieSection(tmdbMovieListsService.getPopularMovieListsMono(page)),
                movieSection(tmdbMovieListsService.getNowPlayingMovieListsMono(page)),
                movieSection(tmdbMovieListsService.getTopRatedMovieListsMono(page)),
                tvSection(tmdbTrendingService.getTrendingTvMono(timeWindow, page)),
                tvSection(tmdbTvSeriesListsService.getPopularTvSeriesListsMono(page)),
                tvSection(tmdbTvSeriesListsService.getOnTheAirTvSeriesListsMono(page)),
                tvSection(tmdbTvSeriesListsService.getTopRatedTvSeriesListsMono(page))
        );
    }

    private static Mono<List<ContentItem>> movieSection(Mono<TmdbMovieListsResponse> response) {
        return isolate(response.map(TmdbDiscoverDtoMapper::toContentItemList));
    }

    private static Mono<List<ContentItem>> tvSection(Mono<TmdbTvSeriesListsResponse> response) {
        return isolate(response.map(TmdbDiscoverDtoMapper::toContentItemList));
    }

    /** 한 섹션의 실패·빈응답을 빈 리스트로 흡수해 zip 전체가 무너지지 않게 한다. */
    private static Mono<List<ContentItem>> isolate(Mono<List<ContentItem>> section) {
        return section
                .onErrorResume(e -> {
                    log.warn("home section failed, falling back to empty - cause: {}", e.getMessage());
                    return Mono.just(List.of());
                })
                .defaultIfEmpty(List.of());
    }

    @SuppressWarnings("unchecked")
    private static List<ContentItem> at(Object[] results, int index) {
        return (List<ContentItem>) results[index];
    }
}
