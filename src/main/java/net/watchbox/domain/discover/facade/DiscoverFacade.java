package net.watchbox.domain.discover.facade;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.dto.list.ContentPageResponse;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.mapper.tmdb.TmdbDiscoverDtoMapper;
import net.watchbox.domain.discover.dto.HomeResponse;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.service.record.ContentRecordQueryService;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import net.watchbox.global.tmdb.service.TmdbMovieListsService;
import net.watchbox.global.tmdb.service.TmdbTrendingService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesListsService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Observed
public class DiscoverFacade {
    private final TmdbMovieListsService tmdbMovieListsService;
    private final TmdbTvSeriesListsService tmdbTvSeriesListsService;
    private final TmdbTrendingService tmdbTrendingService;
    private final ContentRecordQueryService contentRecordQueryService;

    /**
     * 홈 화면 8개 섹션을 한 번에 조회한다. (BFF)
     *
     * <p><b>병렬 fan-out</b>: TMDB 8건을 {@link Mono#zip} 으로 동시에 호출한다.
     * 한 메서드 안에서 blocking 메서드를 8번 부르면 응답시간이 8건의 <i>합</i>이 되어
     * 프론트가 8개 엔드포인트를 병렬 호출하던 기존보다 느려진다. zip 은 가장 느린 1건에 수렴한다.
     *
     * <p><b>섹션별 실패 격리</b>: zip 은 하나만 실패해도 전체가 실패한다. 홈은 일부 섹션이
     * 죽어도 나머지는 보여야 하므로 섹션마다 에러를 빈 리스트로 흡수한다.
     * (프론트의 {@code Promise.allSettled} 가 하던 역할을 백엔드로 옮긴 것)
     *
     * <p><b>개인화 분리</b>: TMDB 호출부는 사용자와 무관해 캐시 대상이 되고,
     * 개인화(withRecord)는 그 밖에서 DB 조회로 얹는다. 섹션마다 조회하지 않고
     * mediaType 당 IN 쿼리 1회로 묶어 8회 → 2회로 줄인다.
     */
    public HomeResponse getHome(String timeWindow, Integer page, boolean withRecord, Member member) {
        Object[] results = Mono.zip(
                List.of(
                        movieSection(tmdbTrendingService.getTrendingMoviesMono(timeWindow, page)),
                        movieSection(tmdbMovieListsService.getPopularMovieListsMono(page)),
                        movieSection(tmdbMovieListsService.getNowPlayingMovieListsMono(page)),
                        movieSection(tmdbMovieListsService.getTopRatedMovieListsMono(page)),
                        tvSection(tmdbTrendingService.getTrendingTvMono(timeWindow, page)),
                        tvSection(tmdbTvSeriesListsService.getPopularTvSeriesListsMono(page)),
                        tvSection(tmdbTvSeriesListsService.getOnTheAirTvSeriesListsMono(page)),
                        tvSection(tmdbTvSeriesListsService.getTopRatedTvSeriesListsMono(page))
                ),
                sections -> sections
        ).block();

        // 순서: [trending, popular, nowShowing, topRated]
        List<List<ContentItem>> movies = List.of(at(results, 0), at(results, 1), at(results, 2), at(results, 3));
        List<List<ContentItem>> tv = List.of(at(results, 4), at(results, 5), at(results, 6), at(results, 7));

        // JPA 는 blocking 이라 이벤트 루프가 아닌 요청 스레드(zip 종료 후)에서 처리한다.
        if (withRecord && member != null) {
            movies = contentRecordQueryService.attachMemberRecordBatch(movies, member, MediaType.MOVIE);
            tv = contentRecordQueryService.attachMemberRecordBatch(tv, member, MediaType.TV);
        }

        return new HomeResponse(
                movies.get(0), tv.get(0),  // trending
                movies.get(1), tv.get(1),  // popular
                movies.get(2), tv.get(2),  // nowShowing
                movies.get(3), tv.get(3)   // topRated
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

    public ContentPageResponse getPopularMovies(Integer page, boolean withRecord, Member member) {
        TmdbMovieListsResponse tmdbResponse = tmdbMovieListsService.getPopularMovieLists(page);
        List<ContentItem> items = TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse);
        if (withRecord && member != null) {
            items = contentRecordQueryService.attachMemberRecord(items, member, MediaType.MOVIE);
        }
        return ContentPageResponse.builder()
                .contentItemList(items)
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getPopularTvSeries(Integer page, boolean withRecord, Member member) {
        TmdbTvSeriesListsResponse tmdbResponse = tmdbTvSeriesListsService.getPopularTvSeriesLists(page);
        List<ContentItem> items = TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse);
        if (withRecord && member != null) {
            items = contentRecordQueryService.attachMemberRecord(items, member, MediaType.TV);
        }
        return ContentPageResponse.builder()
                .contentItemList(items)
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getTopRatedMovies(Integer page, boolean withRecord, Member member) {
        TmdbMovieListsResponse tmdbResponse = tmdbMovieListsService.getTopRatedMovieLists(page);
        List<ContentItem> items = TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse);
        if (withRecord && member != null) {
            items = contentRecordQueryService.attachMemberRecord(items, member, MediaType.MOVIE);
        }
        return ContentPageResponse.builder()
                .contentItemList(items)
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getTopRatedTvSeries(Integer page, boolean withRecord, Member member) {
        TmdbTvSeriesListsResponse tmdbResponse = tmdbTvSeriesListsService.getTopRatedTvSeriesLists(page);
        List<ContentItem> items = TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse);
        if (withRecord && member != null) {
            items = contentRecordQueryService.attachMemberRecord(items, member, MediaType.TV);
        }
        return ContentPageResponse.builder()
                .contentItemList(items)
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getNowPlayingMovies(Integer page, boolean withRecord, Member member) {
        TmdbMovieListsResponse tmdbResponse = tmdbMovieListsService.getNowPlayingMovieLists(page);
        List<ContentItem> items = TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse);
        if (withRecord && member != null) {
            items = contentRecordQueryService.attachMemberRecord(items, member, MediaType.MOVIE);
        }
        return ContentPageResponse.builder()
                .contentItemList(items)
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getOnTheAirTvSeries(Integer page, boolean withRecord, Member member) {
        TmdbTvSeriesListsResponse tmdbResponse = tmdbTvSeriesListsService.getOnTheAirTvSeriesLists(page);
        List<ContentItem> items = TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse);
        if (withRecord && member != null) {
            items = contentRecordQueryService.attachMemberRecord(items, member, MediaType.TV);
        }
        return ContentPageResponse.builder()
                .contentItemList(items)
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getTrendingMovies(String timeWindow, Integer page, boolean withRecord, Member member) {
        TmdbMovieListsResponse tmdbResponse = tmdbTrendingService.getTrendingMovies(timeWindow, page);
        List<ContentItem> items = TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse);
        if (withRecord && member != null) {
            items = contentRecordQueryService.attachMemberRecord(items, member, MediaType.MOVIE);
        }
        return ContentPageResponse.builder()
                .contentItemList(items)
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getTrendingTv(String timeWindow, Integer page, boolean withRecord, Member member) {
        TmdbTvSeriesListsResponse tmdbResponse = tmdbTrendingService.getTrendingTv(timeWindow, page);
        List<ContentItem> items = TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse);
        if (withRecord && member != null) {
            items = contentRecordQueryService.attachMemberRecord(items, member, MediaType.TV);
        }
        return ContentPageResponse.builder()
                .contentItemList(items)
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

}
