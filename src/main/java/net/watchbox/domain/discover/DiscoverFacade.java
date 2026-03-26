package net.watchbox.domain.discover;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.base.dto.list.ContentPageResponse;
import net.watchbox.domain.content.base.mapper.tmdb.TmdbDiscoverDtoMapper;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import net.watchbox.global.tmdb.service.TmdbMovieListsService;
import net.watchbox.global.tmdb.service.TmdbTrendingService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesListsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscoverFacade {
    private final TmdbMovieListsService tmdbMovieListsService;
    private final TmdbTvSeriesListsService tmdbTvSeriesListsService;
    private final TmdbTrendingService tmdbTrendingService;

    public ContentPageResponse getPopularMovies(Integer page, boolean withRecord) {
        TmdbMovieListsResponse tmdbResponse = tmdbMovieListsService.getPopularMovieLists(page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse))
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getPopularTvSeries(Integer page, boolean withRecord) {
        TmdbTvSeriesListsResponse tmdbResponse = tmdbTvSeriesListsService.getPopularTvSeriesLists(page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse))
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getTopRatedMovies(Integer page, boolean withRecord) {
        TmdbMovieListsResponse tmdbResponse = tmdbMovieListsService.getTopRatedMovieLists(page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse))
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getTopRatedTvSeries(Integer page, boolean withRecord) {
        TmdbTvSeriesListsResponse tmdbResponse = tmdbTvSeriesListsService.getTopRatedTvSeriesLists(page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse))
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getNowPlayingMovies(Integer page, boolean withRecord) {
        TmdbMovieListsResponse tmdbResponse = tmdbMovieListsService.getNowPlayingMovieLists(page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse))
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getOnTheAirTvSeries(Integer page, boolean withRecord) {
        TmdbTvSeriesListsResponse tmdbResponse = tmdbTvSeriesListsService.getOnTheAirTvSeriesLists(page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse))
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getTrendingMovies(String timeWindow, Integer page, boolean withRecord) {
        TmdbMovieListsResponse tmdbResponse = tmdbTrendingService.getTrendingMovies(timeWindow, page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse))
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getTrendingTv(String timeWindow, Integer page, boolean withRecord) {
        TmdbMovieListsResponse tmdbResponse = tmdbTrendingService.getTrendingTv(timeWindow, page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse))
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }
}
