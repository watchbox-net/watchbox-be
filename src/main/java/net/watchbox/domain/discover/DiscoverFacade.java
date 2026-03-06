package net.watchbox.domain.discover;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.base.dto.list.ContentPageResponse;
import net.watchbox.domain.content.base.mapper.tmdb.TmdbDiscoverDtoMapper;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import net.watchbox.global.tmdb.service.TmdbMovieListsService;
import net.watchbox.global.tmdb.service.TmdbTvSeriesListsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscoverFacade {
    private final TmdbMovieListsService tmdbMovieListsService;
    private final TmdbTvSeriesListsService tmdbTvSeriesListsService;

    public ContentPageResponse getPopularMovies(Integer page, String region) {
        TmdbMovieListsResponse tmdbResponse = tmdbMovieListsService.getPopularMovieLists(page, region);
        return ContentPageResponse.builder()
                .contentItemList(TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse))
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }

    public ContentPageResponse getPopularTvSeries(Integer page) {
        TmdbTvSeriesListsResponse tmdbResponse = tmdbTvSeriesListsService.getPopularTvSeriesLists(page);
        return ContentPageResponse.builder()
                .contentItemList(TmdbDiscoverDtoMapper.toContentItemList(tmdbResponse))
                .totalCount(tmdbResponse.getTotalResults().longValue())
                .totalPages(tmdbResponse.getTotalPages().longValue())
                .currentPage(tmdbResponse.getPage().longValue())
                .build();
    }
}
