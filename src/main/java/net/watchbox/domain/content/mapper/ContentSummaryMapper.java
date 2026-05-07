package net.watchbox.domain.content.mapper;

import net.watchbox.domain.content.dto.list.ContentSummary;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.sub.movie.entity.Movie;
import net.watchbox.domain.content.sub.person.entity.Person;
import net.watchbox.domain.content.sub.tv.entity.Tv;
import net.watchbox.domain.content.sub.movie.entity.MovieGenre;
import net.watchbox.domain.content.sub.tv.entity.TvGenre;
import net.watchbox.global.util.ConvertUtils;

public class ContentSummaryMapper {
    public static ContentSummary fromContent(Content content) {
        return switch (content.getMediaType()) {
            case MOVIE -> fromMovie(content.getContentId(), content.getMovie());
            case TV -> fromTv(content.getContentId(), content.getTv());
            case PERSON -> fromPerson(content.getContentId(), content.getPerson());
        };
    }

    public static ContentSummary fromMovie(Long contentId, Movie movie) {
        return ContentSummary.builder()
                .contentId(contentId)
                .tmdbId(movie.getTmdbId())
                .mediaType(MediaType.MOVIE)
                .popularity(movie.getPopularity())
                .posterPath(movie.getPosterPath())
                .voteAverage(ConvertUtils.roundVoteAverage(movie.getVoteAverage()))
                .voteCount(movie.getVoteCount())
                .releaseYear(movie.getReleaseDate().getYear())
                .genreList(MovieGenre.mapSummaryGenreIdListToKorean(movie.getGenreIds()))
                .title(movie.getTitleKo())
                .titleOriginal(movie.getTitleOriginal())
                .build();
    }

    public static ContentSummary fromTv(Long contentId, Tv tv) {
        return ContentSummary.builder()
                .contentId(contentId)
                .tmdbId(tv.getTmdbId())
                .mediaType(MediaType.TV)
                .popularity(tv.getPopularity())
                .posterPath(tv.getPosterPath())
                .voteAverage(ConvertUtils.roundVoteAverage(tv.getVoteAverage()))
                .voteCount(tv.getVoteCount())
                .firstAirYear(tv.getFirstAirDate().getYear())
                .lastAirYear(tv.getLastAirDate().getYear())
                .genreList(TvGenre.mapSummaryGenreIdListToKorean(tv.getGenreIds()))
                .name(tv.getNameKo())
                .nameOriginal(tv.getNameOriginal())
                .build();
    }

    public static ContentSummary fromPerson(Long contentId, Person person) {
        return ContentSummary.builder()
                .contentId(contentId)
                .tmdbId(person.getTmdbId())
                .mediaType(MediaType.PERSON)
                .popularity(person.getPopularity())
                .name(person.getNameKo())
                .nameOriginal(person.getNameOriginal())
                .knownForDepartment(person.getKnownForDepartment())
                .profilePath(person.getProfilePath())
                .build();
    }
}
