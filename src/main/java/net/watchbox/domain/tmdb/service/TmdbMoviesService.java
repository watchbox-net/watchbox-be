package net.watchbox.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.tmdb.response.movies.TmdbMovieDetailsResponse;
import net.watchbox.global.properties.TmdbProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class TmdbMoviesService {
    private final WebClient.Builder webClientBuilder;
    private final TmdbProperties tmdbProperties;

    /**
     * Details Get 요청
     * @movie_Id
     */
    public TmdbMovieDetailsResponse getMovieDetails(Long movieId) {
        WebClient webClient = webClientBuilder.baseUrl(tmdbProperties.getApi().getBaseUrl()).build();

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{movieId}")
                        .queryParam("api_key", tmdbProperties.getApi().getKey())
                        .build(movieId))
                .retrieve()
                .bodyToMono(TmdbMovieDetailsResponse.class)
                .block();
    }
}
