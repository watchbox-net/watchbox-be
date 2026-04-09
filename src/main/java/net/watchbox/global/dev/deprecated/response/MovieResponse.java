package net.watchbox.global.dev.deprecated.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.sub.movie.entity.Movie;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResultItem;
import net.watchbox.global.tmdb.util.MovieGenre;

import java.util.List;
import java.util.Objects;

@Getter
@ToString
@Builder
public class MovieResponse {
    private Long id; // tmdbId
    private String title;
    private String titleOriginal;
    private String posterPath;
    private Double popularity;
    private Double voteAverage;
    private Long voteCount;
    private Integer year;
    private List<String> genres;

    public static MovieResponse from(Movie movie){
        List<String> genreNames = movie.getGenreIds().stream()
                .map(MovieGenre::getKoreanNameById)
                .filter(Objects::nonNull)
                .toList();

        return MovieResponse.builder()
                .id(movie.getTmdbId())
                .title(movie.getTitleKo())
                .titleOriginal(movie.getTitleOriginal())
                .posterPath(movie.getPosterPath())
                .popularity(movie.getPopularity())
                .voteAverage(movie.getVoteAverage())
                .voteCount(movie.getVoteCount())
                .year(movie.getYear())
                .genres(genreNames)
                .build();
    }

    public static MovieResponse from(TmdbMovieListsResultItem item) {
        List<String> genreNames = item.getGenreIds().stream()
                .map(MovieGenre::getKoreanNameById)
                .filter(Objects::nonNull)
                .toList();

        Integer year = null;
        if (item.getReleaseDate() != null && item.getReleaseDate().length() >= 4) {
            year = Integer.parseInt(item.getReleaseDate().substring(0, 4));
        }

        return MovieResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .titleOriginal(item.getOriginalTitle())
                .posterPath(item.getPosterPath())
                .popularity(item.getPopularity())
                .voteAverage(item.getVoteAverage())
                .voteCount(item.getVoteCount())
                .year(year)
                .genres(genreNames)
                .build();
    }
}
