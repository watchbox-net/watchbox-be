package net.watchbox.domain.discover;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.base.dto.list.ContentPageResponse;
import net.watchbox.domain.content.base.dto.list.ContentSummary;
import net.watchbox.domain.content.base.mapper.discover.TmdbDiscoverListMapper;
import net.watchbox.domain.content.movie.service.MovieService;
import net.watchbox.domain.content.tv.service.TvService;
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
        TmdbMovieListsResponse tmdbPopularMovieList = tmdbMovieListsService.getPopularMovieLists(page, region);
        return TmdbDiscoverListMapper.toContentPageResponse(tmdbPopularMovieList);
    }

    public ContentPageResponse getPopularTvSeries(Integer page) {
        TmdbTvSeriesListsResponse tmdbPopularTvList = tmdbTvSeriesListsService.getPopularTvSeriesLists(page);
        return TmdbDiscoverListMapper.toContentPageResponse(tmdbPopularTvList);
    }
}
