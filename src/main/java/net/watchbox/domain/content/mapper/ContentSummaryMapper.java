package net.watchbox.domain.content.mapper;

import net.watchbox.domain.content.dto.list.ContentSummary;
import net.watchbox.domain.content.entity.Content;
import net.watchbox.domain.content.entity.MediaType;
import net.watchbox.domain.content.sub.movie.entity.Movie;
import net.watchbox.domain.content.sub.person.entity.Person;
import net.watchbox.domain.content.sub.tv.entity.Tv;
import net.watchbox.global.tmdb.util.MovieGenre;
import net.watchbox.global.tmdb.util.TvGenre;
import net.watchbox.global.util.ConvertUtils;

public class ContentSummaryMapper {
    public static ContentSummary fromContent(Content content) {
        return switch (content.getMediaType()) {
            case MOVIE -> fromMovie(content.getMovie());
            case TV -> fromTv(content.getTv());
            case PERSON -> fromPerson(content.getPerson());
        };
    }

    public static ContentSummary fromMovie(Movie movie) {
        return ContentSummary.builder()
                .contentId(movie.getTmdbId())
                .mediaType(MediaType.MOVIE)
                .popularity(movie.getPopularity())
                .posterPath(movie.getPosterPath())
                .voteAverage(ConvertUtils.roundVoteAverage(movie.getVoteAverage()))
                .voteCount(movie.getVoteCount())
                .year(movie.getYear())
                .genreList(MovieGenre.mapGenreIdListToKorean(movie.getGenreIds()))
                .title(movie.getTitleKo())
                .titleOriginal(movie.getTitleOriginal())
                .build();
    }

    public static ContentSummary fromTv(Tv tv) {
        return ContentSummary.builder()
                .contentId(tv.getTmdbId())
                .mediaType(MediaType.TV)
                .popularity(tv.getPopularity())
                .posterPath(tv.getPosterPath())
                .voteAverage(ConvertUtils.roundVoteAverage(tv.getVoteAverage()))
                .voteCount(tv.getVoteCount())
                .year(tv.getYear())
                .genreList(TvGenre.mapGenreIdListToKorean(tv.getGenreIds()))
                .name(tv.getNameKo())
                .nameOriginal(tv.getNameOriginal())
                .build();
    }

    public static ContentSummary fromPerson(Person person) {
        return ContentSummary.builder()
                .contentId(person.getTmdbId())
                .mediaType(MediaType.PERSON)
                .popularity(person.getPopularity())
                .name(person.getNameKo())
                .nameOriginal(person.getNameOriginal())
                .knownForDepartment(person.getKnownForDepartment())
                .profilePath(person.getProfilePath())
                .build();
    }
}
