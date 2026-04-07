package net.watchbox.domain.content.mapper.tmdb;

import net.watchbox.domain.content.dto.detail.MovieInfo;
import net.watchbox.domain.content.dto.detail.PersonInfo;
import net.watchbox.domain.content.dto.detail.TvInfo;
import net.watchbox.global.tmdb.inner.title.TmdbGenreItem;
import net.watchbox.global.tmdb.response.movies.TmdbMoviesDetailsResponse;
import net.watchbox.global.tmdb.response.people.TmdbPeopleDetailsResponse;
import net.watchbox.global.tmdb.response.tvseries.TmdbTvSeriesDetailsResponse;
import net.watchbox.global.tmdb.util.TmdbUtils;

import java.util.List;

public class TmdbContentDetailDtoMapper {

    public static MovieInfo toMovieInfo(TmdbMoviesDetailsResponse response) {
        return MovieInfo.builder()
                .contentId(response.getId())
                .titleKo(response.getTitle())
                .titleOriginal(response.getOriginalTitle())
                .posterPath(response.getPosterPath())
                .year(TmdbUtils.extractYear(response.getReleaseDate()))
                .genreList(response.getGenres().stream().map(TmdbGenreItem::getName).toList())
                .overview(response.getOverview())
                .backdropPath(response.getBackdropPath())
                .originalLanguage(response.getOriginalLanguage())
                .releaseDate(TmdbUtils.extractDate(response.getReleaseDate()))
                .adult(response.isAdult())
                .video(response.isVideo())
                .status(response.getStatus())
                .runtime(response.getRuntime() != null ? response.getRuntime().intValue() : null)
                .tagline(response.getTagline())
                .homepage(response.getHomepage())
                .budget(response.getBudget())
                .revenue(response.getRevenue())
                .build();
    }

    public static TvInfo toTvInfo(TmdbTvSeriesDetailsResponse response) {
        return TvInfo.builder()
                .contentId(response.getId())
                .nameKo(response.getName())
                .nameOriginal(response.getOriginalName())
                .posterPath(response.getPosterPath())
                .popularity(response.getPopularity())
                .year(TmdbUtils.extractYear(response.getFirstAirDate()))
                .genreList(response.getGenres().stream().map(TmdbGenreItem::getName).toList())
                .overview(response.getOverview())
                .backdropPath(response.getBackdropPath())
                .originalLanguage(response.getOriginalLanguage())
                .firstAirDate(TmdbUtils.extractDate(response.getFirstAirDate()))
                .adult(response.isAdult())
                .status(response.getStatus())
                .type(response.getType())
                .tagline(response.getTagline())
                .homepage(response.getHomepage())
                .numberOfEpisodes(response.getNumberOfEpisodes())
                .numberOfSeasons(response.getNumberOfSeasons())
                .lastAirDate(TmdbUtils.extractDate(response.getLastAirDate()))
                .build();
    }

    public static PersonInfo toPersonInfo(TmdbPeopleDetailsResponse response) {
        return PersonInfo.builder()
                .contentId(response.getId())
                .nameKo(response.getName())
                .profilePath(response.getProfilePath())
                .knownForDepartment(response.getKnownForDepartment())
                .popularity(response.getPopularity())
                .knownForList(List.of()) // TMDB Details API에 known_for 없음 - 추가 API 필요
                .biography(response.getBiography())
                .gender(response.getGender())
                .birthday(TmdbUtils.extractDate(response.getBirthday()))
                .deathday(TmdbUtils.extractDate(response.getDeathday()))
                .placeOfBirth(response.getPlaceOfBirth())
                .homepage(response.getHomepage())
                .adult(response.isAdult())
                .build();
    }

}
