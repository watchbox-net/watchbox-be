package net.watchbox.domain.discover.facade;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.dto.list.ContentPageResponse;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.mapper.tmdb.TmdbDiscoverDtoMapper;
import net.watchbox.domain.discover.dto.HomeResponse;
import net.watchbox.domain.discover.warmup.HomeTmdbSectionLoader;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.service.record.ContentRecordQueryService;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import net.watchbox.global.tmdb.service.TmdbMovieListsService;
import net.watchbox.global.tmdb.service.TmdbTrendingService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesListsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Observed
public class DiscoverFacade {
    private final TmdbMovieListsService tmdbMovieListsService;
    private final TmdbTvSeriesListsService tmdbTvSeriesListsService;
    private final TmdbTrendingService tmdbTrendingService;
    private final ContentRecordQueryService contentRecordQueryService;
    private final HomeTmdbSectionLoader homeTmdbSectionLoader;

    /**
     * 홈 화면 8개 섹션을 한 번에 조회한다. (BFF)
     *
     * <p>TMDB 병렬 조회·실패 격리는 {@link HomeTmdbSectionLoader} 가 담당하고, 여기서는
     * <b>개인화를 얹어 응답을 조립</b>하는 일만 한다.
     *
     * <p><b>개인화 분리</b>: TMDB 호출부는 사용자와 무관해 캐시 대상이 되고,
     * 개인화(withRecord)는 그 밖에서 DB 조회로 얹는다. 섹션마다 조회하지 않고
     * mediaType 당 IN 쿼리 1회로 묶어 8회 → 2회로 줄인다.
     */
    public HomeResponse getHome(String timeWindow, Integer page, boolean withRecord, Member member) {
        HomeTmdbSectionLoader.Sections sections = homeTmdbSectionLoader.load(timeWindow, page);

        // 순서: [trending, popular, nowShowing, topRated]
        List<List<ContentItem>> movies = sections.movies();
        List<List<ContentItem>> tv = sections.tv();

        // JPA 는 blocking 이라 이벤트 루프가 아닌 요청 스레드(조회 종료 후)에서 처리한다.
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
