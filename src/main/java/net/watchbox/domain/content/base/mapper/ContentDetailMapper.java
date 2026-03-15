package net.watchbox.domain.content.base.mapper;

import net.watchbox.domain.content.base.dto.detail.MovieInfo;
import net.watchbox.domain.content.base.dto.detail.PersonInfo;
import net.watchbox.domain.content.base.dto.detail.TvInfo;
import net.watchbox.domain.content.movie.entity.Movie;
import net.watchbox.domain.content.movie.entity.MovieDetail;
import net.watchbox.domain.content.person.entity.Person;
import net.watchbox.domain.content.person.entity.PersonDetail;
import net.watchbox.domain.content.tv.entity.Tv;
import net.watchbox.domain.content.tv.entity.TvDetail;
import net.watchbox.global.tmdb.util.MovieGenre;
import net.watchbox.global.util.JsonParseUtil;

public class ContentDetailMapper {
    public static MovieInfo fromMovie(Movie movie) {
        MovieDetail movieDetail = movie.getMovieDetail();
        return MovieInfo.builder()
                .contentId(movie.getTmdbId())
                .titleKo(movie.getTitleKo())
                .titleOriginal(movie.getTitleOriginal())
                .posterPath(movie.getPosterPath())
                .year(movie.getYear())
                .genreList(MovieGenre.mapGenreIdListToKorean(movie.getGenreIds()))

                .overview(movieDetail.getOverview())
                .backdropPath(movieDetail.getBackdropPath())
                .originalLanguage(movieDetail.getOriginalLanguage())
                .releaseDate(movieDetail.getReleaseDate())
                .adult(movieDetail.getAdult())
                .video(movieDetail.getVideo())
                .status(movieDetail.getStatus())
                .runtime(movieDetail.getRuntime())
                .tagline(movieDetail.getTagline())
                .homepage(movieDetail.getHomepage())
                .budget(movieDetail.getBudget())
                .revenue(movieDetail.getRevenue())
                .originCountryList(JsonParseUtil.toStringList(movieDetail.getOriginCountries()))
                .build();
    }

    public static TvInfo fromTv(Tv tv) {
        TvDetail tvDetail = tv.getTvDetail();
        return TvInfo.builder()
                .contentId(tv.getTmdbId())
                .nameKo(tv.getNameKo())
                .nameOriginal(tv.getNameOriginal())
                .posterPath(tv.getPosterPath())
                .year(tv.getYear())
                .genreList(MovieGenre.mapGenreIdListToKorean(tv.getGenreIds()))

                .overview(tvDetail.getOverview())
                .backdropPath(tvDetail.getBackdropPath())
                .originalLanguage(tvDetail.getOriginalLanguage())
                .firstAirDate(tvDetail.getFirstAirDate())
                .adult(tvDetail.getAdult())
                .video(tvDetail.getVideo())
                .status(tvDetail.getStatus())
                .type(tvDetail.getType())
                .tagline(tvDetail.getTagline())
                .homepage(tvDetail.getHomepage())
                .budget(tvDetail.getBudget())
                .revenue(tvDetail.getRevenue())
                .numberOfEpisodes(tvDetail.getNumberOfEpisodes())
                .numberOfSeasons(tvDetail.getNumberOfSeasons())
                .lastAirDate(tvDetail.getLastAirDate())
                .languageList(JsonParseUtil.toStringList(tvDetail.getSpokenLanguages()))
                .originCountryList(JsonParseUtil.toStringList(tvDetail.getOriginCountries()))
                .build();
    }

    public static PersonInfo fromPerson(Person person) {
        PersonDetail personDetail = person.getPersonDetail();
        return PersonInfo.builder()
                .contentId(person.getTmdbId())
                .nameKo(person.getNameKo())
                .nameOriginal(person.getNameOriginal())
                .profilePath(person.getProfilePath())
                .knownForDepartment(person.getKnownForDepartment())
                .popularity(person.getPopularity())

                .biography(personDetail.getBiography())
                .gender(personDetail.getGender())
                .birthday(personDetail.getBirthday())
                .deathday(personDetail.getDeathday())
                .placeOfBirth(personDetail.getPlaceOfBirth())
                .homepage(personDetail.getHomepage())
                .adult(personDetail.getAdult())
                .build();
    }
}
