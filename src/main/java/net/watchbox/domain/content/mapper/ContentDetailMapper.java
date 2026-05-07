package net.watchbox.domain.content.mapper;

import net.watchbox.domain.content.dto.detail.MovieInfo;
import net.watchbox.domain.content.dto.detail.PersonInfo;
import net.watchbox.domain.content.dto.detail.TvInfo;
import net.watchbox.domain.content.sub.movie.entity.Movie;
import net.watchbox.domain.content.sub.movie.entity.MovieDetail;
import net.watchbox.domain.content.sub.person.entity.Person;
import net.watchbox.domain.content.sub.tv.entity.Tv;
import net.watchbox.domain.content.sub.tv.entity.TvDetail;
import net.watchbox.global.tmdb.util.MovieGenre;

public class ContentDetailMapper {
    public static MovieInfo fromMovie(Movie movie) {
        MovieDetail movieDetail = movie.getMovieDetail();
        return MovieInfo.builder()
                .contentId(movie.getContent().getContentId())
                .tmdbId(movie.getTmdbId())
                .titleKo(movie.getTitleKo())
                .titleOriginal(movie.getTitleOriginal())
                .posterPath(movie.getPosterPath())
                .year(movie.getYear())
                .genreList(MovieGenre.mapSummaryGenreIdListToKorean(movie.getGenreIds()))

                .overview(movieDetail.getOverview())
                .backdropPath(movieDetail.getBackdropPath())
//                .originalLanguage(movieDetail.getOriginalLanguage())
                .releaseDate(movieDetail.getReleaseDate())
//                .adult(movieDetail.getAdult())
//                .video(movieDetail.getVideo())
//                .status(movieDetail.getStatus())
                .runtime(movieDetail.getRuntime())
//                .tagline(movieDetail.getTagline())
//                .homepage(movieDetail.getHomepage())
//                .budget(movieDetail.getBudget())
//                .revenue(movieDetail.getRevenue())
                .build();
    }

    public static TvInfo fromTv(Tv tv) {
        TvDetail tvDetail = tv.getTvDetail();
        return TvInfo.builder()
                .contentId(tv.getContent().getContentId())
                .tmdbId(tv.getTmdbId())
                .nameKo(tv.getNameKo())
                .nameOriginal(tv.getNameOriginal())
                .posterPath(tv.getPosterPath())
                .firstYear(tv.getFirstAirDate().getYear())
                .lastYear(tv.getLastAirDate().getYear())
                .genreList(MovieGenre.mapSummaryGenreIdListToKorean(tv.getGenreIds()))

                .overview(tvDetail.getOverview())
                .backdropPath(tvDetail.getBackdropPath())
//                .originalLanguage(tvDetail.getOriginalLanguage())
                .firstAirDate(tvDetail.getFirstAirDate())
//                .adult(tvDetail.getAdult())
//                .video(tvDetail.getVideo())
                .status(tvDetail.getStatus())
//                .type(tvDetail.getType())
                .tagline(tvDetail.getTagline())
//                .homepage(tvDetail.getHomepage())
//                .budget(tvDetail.getBudget())
//                .revenue(tvDetail.getRevenue())
//                .numberOfEpisodes(tvDetail.getNumberOfEpisodes())
                .numberOfSeasons(tvDetail.getNumberOfSeasons())
                .lastAirDate(tvDetail.getLastAirDate())
                .build();
    }

    public static PersonInfo fromPerson(Person person) {
        return PersonInfo.builder()
                .contentId(person.getContent().getContentId())
                .tmdbId(person.getTmdbId())
                .nameKo(person.getNameKo())
                .nameOriginal(person.getNameOriginal())
                .profilePath(person.getProfilePath())
                .knownForDepartment(person.getKnownForDepartment())
                .popularity(person.getPopularity())
                .birthday(person.getBirthday())
                .placeOfBirth(person.getPlaceOfBirth())
                .build();
    }
}
