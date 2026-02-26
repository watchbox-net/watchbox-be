package net.watchbox.domain.content.movie.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;

import java.util.List;

@Getter
@ToString
@Builder
public class MovieListResponse {
    private List<MovieResponse> movieList;
    private Integer page;
    private Integer totalPages;
    private Integer totalResults;

    public static MovieListResponse from(TmdbMovieListsResponse response) {
        List<MovieResponse> movieList = response.getResults().stream()
                .map(MovieResponse::from)
                .toList();

        return MovieListResponse.builder()
                .page(response.getPage())
                .totalPages(response.getTotalPages())
                .totalResults(response.getTotalResults())
                .movieList(movieList)
                .build();
    }
}
