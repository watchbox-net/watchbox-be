package net.watchbox.domain.content.mapper.tmdb;

import net.watchbox.domain.content.dto.detail.MovieInfo;
import net.watchbox.domain.content.dto.detail.PersonInfo;
import net.watchbox.domain.content.dto.detail.TvInfo;
import net.watchbox.global.tmdb.configuration.Department;
import net.watchbox.global.tmdb.response.common.TmdbWorkImagesResponse;
import net.watchbox.global.tmdb.response.movies.TmdbMoviesDetailsResponse;
import net.watchbox.global.tmdb.response.people.TmdbPersonDetailsResponse;
import net.watchbox.global.tmdb.response.tvseries.TmdbTvSeriesDetailsResponse;
import net.watchbox.domain.content.sub.movie.entity.MovieGenre;
import net.watchbox.global.tmdb.configuration.Country;
import net.watchbox.global.tmdb.util.TmdbUtils;
import net.watchbox.domain.content.sub.tv.entity.TvGenre;

public class TmdbContentDetailDtoMapper {

    public static MovieInfo toMovieInfo(TmdbMoviesDetailsResponse detailsResponse, TmdbWorkImagesResponse imagesResponse) {
        return MovieInfo.builder()
                .tmdbId(detailsResponse.getId())
                .titleKo(detailsResponse.getTitle())
                .titleOriginal(detailsResponse.getOriginalTitle())
                .posterPath(detailsResponse.getPosterPath())
                .year(TmdbUtils.extractYear(detailsResponse.getReleaseDate()))
                .genreList(MovieGenre.mapDetailGenreIdListToKorean(
                        detailsResponse.getGenres().stream()
                                .map(g -> g.getId().intValue())
                                .toList()))
                .overview(detailsResponse.getOverview())
                .backdropPath(detailsResponse.getBackdropPath())
                .releaseDate(TmdbUtils.extractDate(detailsResponse.getReleaseDate()))
                .originCountry(detailsResponse.getOriginCountry().stream().findFirst()
                        .map(Country::getKoreanByCode).orElse(null))
                .runtime(detailsResponse.getRuntime() != null ? detailsResponse.getRuntime().intValue() : null)
                // append_to_response
                .personCredit(TmdbAppendToResponseConverter.toPersonCredit(detailsResponse.getCredits()))
                .watchProviderList(TmdbAppendToResponseConverter.toWatchProviderList(detailsResponse.getWatchProviders()))
                .backdropPathList(TmdbAppendToResponseConverter.toBackdropPathList(imagesResponse))
//                .video(detailsResponse.isVideo())
                // 미사용 필드
                .popularity(detailsResponse.getPopularity())
                .status(detailsResponse.getStatus())
                .tagline(detailsResponse.getTagline())
//                .originalLanguage(detailsResponse.getOriginalLanguage())
//                .homepage(detailsResponse.getHomepage())
//                .budget(detailsResponse.getBudget())
//                .revenue(detailsResponse.getRevenue())
                .build();
    }

    public static TvInfo toTvInfo(TmdbTvSeriesDetailsResponse detailsResponse, TmdbWorkImagesResponse imagesResponse) {
        return TvInfo.builder()
                .tmdbId(detailsResponse.getId())
                .nameKo(detailsResponse.getName())
                .nameOriginal(detailsResponse.getOriginalName())
                .posterPath(detailsResponse.getPosterPath())
                .firstYear(TmdbUtils.extractYear(detailsResponse.getFirstAirDate()))
                .lastYear(TmdbUtils.extractYear(detailsResponse.getLastAirDate()))
                .originCountry(detailsResponse.getOriginCountry().stream().findFirst()
                        .map(Country::getKoreanByCode).orElse(null))
                .genreList(TvGenre.mapDetailGenreIdListToKorean(
                        detailsResponse.getGenres().stream()
                                .map(g -> g.getId().intValue())
                                .toList()))
                .overview(detailsResponse.getOverview())
                .backdropPath(detailsResponse.getBackdropPath())
                .firstAirDate(TmdbUtils.extractDate(detailsResponse.getFirstAirDate()))
                .lastAirDate(TmdbUtils.extractDate(detailsResponse.getLastAirDate()))
                .numberOfSeasons(detailsResponse.getNumberOfSeasons())
                .inProduction(detailsResponse.isInProduction())
                // append_to_response
                .personCredit(TmdbAppendToResponseConverter.toAggregatePersonCredit(detailsResponse.getAggregateCredits()))
                .watchProviderList(TmdbAppendToResponseConverter.toWatchProviderList(detailsResponse.getWatchProviders()))
                .backdropPathList(TmdbAppendToResponseConverter.toBackdropPathList(imagesResponse))
                // 미사용 필드
                .popularity(detailsResponse.getPopularity())
                .status(detailsResponse.getStatus())
                .tagline(detailsResponse.getTagline())
//                .originalLanguage(response.getOriginalLanguage())
//                .type(response.getType())
//                .homepage(response.getHomepage())
//                .numberOfEpisodes(response.getNumberOfEpisodes())
                .build();
    }

    public static PersonInfo toPersonInfo(TmdbPersonDetailsResponse response, String nameEn, String nameOriginal) {
        return PersonInfo.builder()
                .tmdbId(response.getId())
                .nameKo(response.getName())
                .nameOriginal(nameOriginal)
                .nameEn(nameEn == null || nameEn.equals(nameOriginal) ? null : nameEn) // null 이거나 nameOriginal 과 같으면 null
                .profilePath(response.getProfilePath())
                .knownForDepartment(Department.fromEnglishValue(response.getKnownForDepartment()))
                .biography(response.getBiography())
                .birthday(TmdbUtils.extractDate(response.getBirthday()))
                .placeOfBirth(response.getPlaceOfBirth())
                // append_to_response
                .workCredit(TmdbAppendToResponseConverter.toWorkCredit(response.getCombinedCredits()))
                .profilePathList(TmdbAppendToResponseConverter.toProfilePathList(response.getImages()))
                .age(TmdbUtils.calculateAge(response.getBirthday(), response.getDeathday()))
                // 미사용 필드
                .popularity(response.getPopularity())
//                .knownForList(List.of()) // TMDB Details API에 known_for 없음 - 추가 API 필요
                .gender(response.getGender())
                .homepage(response.getHomepage())
                .deathday(TmdbUtils.extractDate(response.getDeathday()))
//                .adult(response.isAdult())
                .build();
    }

}
