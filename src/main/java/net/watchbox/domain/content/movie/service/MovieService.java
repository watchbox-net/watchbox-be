package net.watchbox.domain.content.movie.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.content.movie.dto.response.MovieListResponse;
import net.watchbox.domain.tmdb.response.movielists.TmdbMovieListsResponse;
import net.watchbox.domain.tmdb.service.TmdbMovieListsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MovieService {
    private final TmdbMovieListsService tmdbMovieListsService;

    public MovieListResponse getPopularMovies(Integer page, String region) {
        TmdbMovieListsResponse tmdbPopularMovieList = tmdbMovieListsService.getPopularMovieLists(page, region);
        return MovieListResponse.from(tmdbPopularMovieList);
    }
}
