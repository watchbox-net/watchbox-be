package net.watchbox.domain.content.tv.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.tv.dto.response.TvListResponse;
import net.watchbox.domain.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import net.watchbox.domain.tmdb.service.TmdbTvSeriesListsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TvService {
    private final TmdbTvSeriesListsService tmdbTvSeriesListsService;

    public TvListResponse getPopularTvShows(Integer page) {
        TmdbTvSeriesListsResponse tmdbPopularTvList = tmdbTvSeriesListsService.getPopularTvSeriesLists(page);
        return TvListResponse.from(tmdbPopularTvList);
    }
}
