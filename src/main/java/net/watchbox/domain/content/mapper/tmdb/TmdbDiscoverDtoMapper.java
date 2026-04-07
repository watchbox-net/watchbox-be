package net.watchbox.domain.content.mapper.tmdb;

import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.dto.list.ContentSummary;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResultItem;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import net.watchbox.global.tmdb.response.tvserieslists.TmdbTvSeriesListsResultItem;
import net.watchbox.global.tmdb.util.TmdbUtils;

import java.util.List;

public class TmdbDiscoverDtoMapper {
    // 비로그인 사용자의 순수 TMDB 컨텐츠 리스트 결과 변환
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
                .year(TmdbUtils.extractYear(item.getReleaseDate()))
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
                .year(TmdbUtils.extractYear(item.getFirstAirDate()))
                .name(item.getName())
                .nameOriginal(item.getOriginalName())
                .build();
    }
}
