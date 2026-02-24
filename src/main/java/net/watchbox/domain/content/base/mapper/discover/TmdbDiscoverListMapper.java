package net.watchbox.domain.content.base.mapper.discover;

import net.watchbox.domain.content.base.dto.list.ContentItem;
import net.watchbox.domain.content.base.dto.list.ContentSummary;
import net.watchbox.domain.content.base.entity.MediaType;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResultItem;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResultItem;

import java.util.List;

public class TmdbDiscoverListMapper {
    // Discover가 늘어나면 현재 페이지도 분리해야

    // -------------- TmdbMovieListsResponse -----------------
    public static List<ContentItem> toContentItemList(TmdbMovieListsResponse response) {
        return response.getResults().stream()
                .map(item -> ContentItem.builder()
                        .contentSummary(toMovieSummary(item))
                        .build())
                .toList();
    }

    private static ContentSummary toMovieSummary(TmdbMovieListsResultItem item) {
        return ContentSummary.builder()
                .contentId(item.getId())
                .mediaType(MediaType.MOVIE)
                .popularity(item.getPopularity())
                .posterPath(item.getPosterPath())
                .voteAverage(item.getVoteAverage())
                .voteCount(item.getVoteCount())
                .year(Integer.parseInt(item.getReleaseDate().substring(0, 4)))
                .title(item.getTitle())
                .titleOriginal(item.getOriginalTitle())
                .build();
    }

    // -------------- TmdbTvSeriesListsResponse -----------------
    public static List<ContentItem> toContentItemList(TmdbTvSeriesListsResponse response) {
        return response.getResults().stream()
                .map(item -> ContentItem.builder()
                        .contentSummary(toTvSummary(item))
                        .build())
                .toList();
    }

    private static ContentSummary toTvSummary(TmdbTvSeriesListsResultItem item) {
        return ContentSummary.builder()
                .contentId(item.getId())
                .mediaType(MediaType.TV)
                .popularity(item.getPopularity())
                .posterPath(item.getPosterPath())
                .voteAverage(item.getVoteAverage())
                .voteCount(item.getVoteCount())
                .year(Integer.parseInt(item.getFirstAirDate().substring(0, 4)))
                .name(item.getName())
                .nameOriginal(item.getOriginalName())
                .build();
    }
}
