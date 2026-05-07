package net.watchbox.domain.content.mapper.tmdb;

import lombok.extern.slf4j.Slf4j;
import net.watchbox.domain.content.dto.list.ContentItem;
import net.watchbox.domain.content.dto.list.ContentSummary;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.search.SearchType;
import net.watchbox.global.tmdb.inner.search.TmdbSearchResultItem;
import net.watchbox.global.tmdb.response.search.TmdbSearchCommonResponse;
import net.watchbox.domain.content.sub.movie.entity.MovieGenre;
import net.watchbox.domain.content.sub.tv.entity.TvGenre;
import net.watchbox.global.tmdb.util.TmdbUtils;

import java.util.List;

@Slf4j
public class TmdbSearchDtoMapper {
    // (기존) Multi, Movie, Tv, Person 어떤 검색을 하든 List<MultiSearchResponse>로 반환하던거
    // (개정) ~ 어떤 검색을 하든 List<ContentItem>로 반환하도록 변경

    // -------------- TmdbSearchCommonResponse --------------
    public static List<ContentItem> toContentItemList(TmdbSearchCommonResponse response, SearchType searchType) {
        return response.getResults().stream()
                .map(item -> ContentItem.builder()
                        .contentSummary(switch (searchType) {
                            case MULTI -> switch (item.getMediaType()) { // item.getMediaType()은 tmdb 응답의 문자열 반환
                                case "movie" -> toMovieSummary(item);
                                case "tv" -> toTvSummary(item);
                                case "person" -> toPersonSummary(item);
                                default -> {
                                    log.info("ID: {}, Unknown media type: {}", item.getId(), item.getMediaType());
                                    yield null;
                                }
                            };
                            case MOVIE -> toMovieSummary(item);
                            case TV -> toTvSummary(item);
                            case PERSON -> toPersonSummary(item);
                        })
                        .build())
                .toList();
    }

    public static ContentSummary toMovieSummary(TmdbSearchResultItem item) {
        return ContentSummary.builder()
                .tmdbId(item.getId())
                .mediaType(MediaType.MOVIE)
                .popularity(item.getPopularity())
                .posterPath(item.getPosterPath())
                .voteAverage(item.getVoteAverage())
                .voteCount(item.getVoteCount())
                .releaseYear(TmdbUtils.extractYear(item.getReleaseDate()))
                .genreList(MovieGenre.mapSummaryGenreIdListToKorean(item.getGenreIds()))
                .title(item.getTitle())
                .titleOriginal(item.getOriginalTitle())
                .build();
    }

    public static ContentSummary toTvSummary(TmdbSearchResultItem item) {
        return ContentSummary.builder()
                .tmdbId(item.getId())
                .mediaType(MediaType.TV)
                .popularity(item.getPopularity())
                .posterPath(item.getPosterPath())
                .voteAverage(item.getVoteAverage())
                .voteCount(item.getVoteCount())
                .firstAirYear(TmdbUtils.extractYear(item.getFirstAirDate()))
                .lastAirYear(TmdbUtils.extractYear(item.getLastAirDate()))
                .genreList(TvGenre.mapSummaryGenreIdListToKorean(item.getGenreIds()))
                .name(item.getName())
                .nameOriginal(item.getOriginalName())
                .build();
    }

    public static ContentSummary toPersonSummary(TmdbSearchResultItem item) {
        return ContentSummary.builder()
                .tmdbId(item.getId())
                .mediaType(MediaType.PERSON)
                .popularity(item.getPopularity())
                .profilePath(item.getProfilePath())
                .name(item.getName())
                .nameOriginal(item.getOriginalName())
//                .knownForDepartment(item.getKnownForDepartment()) 없나?
                .build();
    }
}
