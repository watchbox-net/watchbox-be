package net.watchbox.domain.discover;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.dto.list.ContentPageResponse;
import net.watchbox.domain.content.mapper.tmdb.TmdbDiscoverDtoMapper;
import net.watchbox.domain.member.entity.Member;
import net.watchbox.domain.record.service.ContentRecordQueryService;
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

    public ContentPageResponse getPopularMovies(Integer page, boolean withRecord, Member member) {
        TmdbMovieListsResponse tmdbResponse = tmdbMovieListsService.getPopularMovieLists(page);
        List<ContentItem> items = TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse);
        if (withRecord && member != null) {
            items = contentRecordQueryService.attachMemberRecord(items, member);
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
            items = contentRecordQueryService.attachMemberRecord(items, member);
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
            items = contentRecordQueryService.attachMemberRecord(items, member);
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
            items = contentRecordQueryService.attachMemberRecord(items, member);
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
            items = contentRecordQueryService.attachMemberRecord(items, member);
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
            items = contentRecordQueryService.attachMemberRecord(items, member);
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
            items = contentRecordQueryService.attachMemberRecord(items, member);
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
            items = contentRecordQueryService.attachMemberRecord(items, member);
        }
        return ContentPageResponse.builder()
                .contentItemList(items)
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

}
