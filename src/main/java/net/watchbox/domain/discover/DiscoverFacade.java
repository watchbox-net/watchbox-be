package net.watchbox.domain.discover;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.base.dto.list.ContentPageResponse;
import net.watchbox.domain.content.base.mapper.discover.TmdbDiscoverListMapper;
import net.watchbox.domain.tmdb.response.movielists.TmdbMovieListsResponse;
import net.watchbox.domain.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import net.watchbox.domain.tmdb.service.TmdbMovieListsService;
import net.watchbox.domain.tmdb.service.TmdbTvSeriesListsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscoverFacade {
    private final TmdbMovieListsService tmdbMovieListsService;
    private final TmdbTvSeriesListsService tmdbTvSeriesListsService;

    public ContentPageResponse getPopularMovies(Integer page, String region) {
        TmdbMovieListsResponse response = tmdbMovieListsService.getPopularMovieLists(page, region);
        return ContentPageResponse.builder()
                .contentItemList(TmdbDiscoverListMapper.toContentItemList(response))
                .totalCount(response.getTotalResults())
                .totalPages(response.getTotalPages())
                .currentPage(response.getPage())
                .build();

    }

    public ContentPageResponse getPopularTvSeries(Integer page) {
        TmdbTvSeriesListsResponse response = tmdbTvSeriesListsService.getPopularTvSeriesLists(page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbDiscoverListMapper.toContentItemList(response))
                .totalCount(response.getTotalResults())
                .totalPages(response.getTotalPages())
                .currentPage(response.getPage())
                .build();
    }
}
