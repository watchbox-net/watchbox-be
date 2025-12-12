package net.watchbox.domain.content.movie.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.domain.content.movie.entity.Movie;

import java.util.List;

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
    private List<Integer> genreIds;

    public static MovieResponse from(Movie movie){
        return MovieResponse.builder()
                .id(movie.getTmdbId())
                .title(movie.getTitleKo())
                .titleOriginal(movie.getTitleOriginal())
                .posterPath(movie.getPosterPath())
                .popularity(movie.getPopularity())
                .voteAverage(movie.getVoteAverage())
                .voteCount(movie.getVoteCount())
                .year(movie.getYear())
                .genreIds(movie.getGenreIds())
                .build();
    }
}
