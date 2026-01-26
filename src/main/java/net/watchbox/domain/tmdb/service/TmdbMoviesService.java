package net.watchbox.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.tmdb.client.TmdbClient;
import net.watchbox.domain.tmdb.response.movies.TmdbMoviesDetailsResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TmdbMoviesService {
    private final TmdbClient tmdbClient;

    /**
     * Details Get 요청
     * @movie_Id
     */
    public TmdbMoviesDetailsResponse getMovieDetails(Long movieId) {

        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/movie/{movieId}")
                        .build(movieId))
                .retrieve()
                .bodyToMono(TmdbMoviesDetailsResponse.class)
                .block();
    }

}
