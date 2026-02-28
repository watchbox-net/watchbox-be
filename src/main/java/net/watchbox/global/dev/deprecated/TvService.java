//package net.watchbox.global.deprecated;
//
//import lombok.RequiredArgsConstructor;
//import net.watchbox.global.dev.deprecated.response.TvListResponse;
//import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
//import net.watchbox.global.tmdb.service.TmdbTvSeriesListsService;
//import org.springframework.stereotype.Service;
//
//@RequiredArgsConstructor
//@Service
//public class TvService {
//    private final TmdbTvSeriesListsService tmdbTvSeriesListsService;
//
//    public TvListResponse getPopularTvShows(Integer page) {
//        TmdbTvSeriesListsResponse tmdbPopularTvList = tmdbTvSeriesListsService.getPopularTvSeriesLists(page);
//        return TvListResponse.from(tmdbPopularTvList);
//    }
//}
