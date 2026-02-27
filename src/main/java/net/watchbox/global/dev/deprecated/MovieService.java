//package net.watchbox.global.deprecated;
//
//import lombok.RequiredArgsConstructor;
//import net.watchbox.domain.content.movie.dto.response.MovieListResponse;
//import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
//import net.watchbox.global.tmdb.service.TmdbMovieListsService;
//import org.springframework.stereotype.Service;
//
//@RequiredArgsConstructor
//@Service
//public class MovieService {
//    private final TmdbMovieListsService tmdbMovieListsService;
//
//    public MovieListResponse getPopularMovies(Integer page, String region) {
//        TmdbMovieListsResponse tmdbPopularMovieList = tmdbMovieListsService.getPopularMovieLists(page, region);
//
//        return MovieListResponse.from(tmdbPopularMovieList);
//    }
//}
