package net.watchbox.global.tmdb.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import net.watchbox.global.tmdb.client.TmdbClient;
import net.watchbox.global.tmdb.response.movies.TmdbMoviesDetailsResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Observed
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
                        .queryParam("append_to_response", "credits,videos,watch/providers")
                        .path("/movie/{movieId}")
                        .build(movieId))
                .retrieve()
                .bodyToMono(TmdbMoviesDetailsResponse.class)
                .block();
    }

}
