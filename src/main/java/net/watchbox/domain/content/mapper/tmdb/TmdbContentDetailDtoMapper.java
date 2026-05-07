package net.watchbox.domain.content.mapper.tmdb;

import net.watchbox.domain.content.dto.detail.MovieInfo;
import net.watchbox.domain.content.dto.detail.PersonInfo;
import net.watchbox.domain.content.dto.detail.TvInfo;
import net.watchbox.global.tmdb.inner.title.TmdbGenreItem;
import net.watchbox.global.tmdb.response.common.TmdbWorkImagesResponse;
import net.watchbox.global.tmdb.response.movies.TmdbMoviesDetailsResponse;
import net.watchbox.global.tmdb.response.people.TmdbPersonDetailsResponse;
import net.watchbox.global.tmdb.response.tvseries.TmdbTvSeriesDetailsResponse;
import net.watchbox.global.tmdb.util.TmdbUtils;

import java.util.List;

public class TmdbContentDetailDtoMapper {

    public static MovieInfo toMovieInfo(TmdbMoviesDetailsResponse detailsResponse, TmdbWorkImagesResponse imagesResponse) {
        return MovieInfo.builder()
                .tmdbId(detailsResponse.getId())
                .titleKo(detailsResponse.getTitle())
                .titleOriginal(detailsResponse.getOriginalTitle())
                .posterPath(detailsResponse.getPosterPath())
                .year(TmdbUtils.extractYear(detailsResponse.getReleaseDate()))
                .genreList(detailsResponse.getGenres().stream().map(TmdbGenreItem::getName).toList())
                .overview(detailsResponse.getOverview())
                .backdropPath(detailsResponse.getBackdropPath())
                .originalLanguage(detailsResponse.getOriginalLanguage())
                .releaseDate(TmdbUtils.extractDate(detailsResponse.getReleaseDate()))
                .adult(detailsResponse.isAdult())
                .video(detailsResponse.isVideo())
                .status(detailsResponse.getStatus())
                .runtime(detailsResponse.getRuntime() != null ? detailsResponse.getRuntime().intValue() : null)
                .tagline(detailsResponse.getTagline())
                .homepage(detailsResponse.getHomepage())
                .budget(detailsResponse.getBudget())
                .revenue(detailsResponse.getRevenue())
                .build();
    }

    public static TvInfo toTvInfo(TmdbTvSeriesDetailsResponse response) {
        return TvInfo.builder()
                .tmdbId(response.getId())
                .nameKo(response.getName())
                .nameOriginal(response.getOriginalName())
                .posterPath(response.getPosterPath())
                .popularity(response.getPopularity())
                .firstYear(TmdbUtils.extractYear(response.getFirstAirDate()))
                .lastYear(TmdbUtils.extractYear(response.getLastAirDate()))
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

    public static PersonInfo toPersonInfo(TmdbPersonDetailsResponse response) {
        return PersonInfo.builder()
                .tmdbId(response.getId())
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
