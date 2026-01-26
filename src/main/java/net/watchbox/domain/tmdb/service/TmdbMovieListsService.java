package net.watchbox.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.tmdb.client.TmdbClient;
import net.watchbox.domain.tmdb.response.movielists.TmdbMovieListsResponse;
import net.watchbox.global.properties.TmdbProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class TmdbMovieListsService {
    private final TmdbClient tmdbClient;

    /**
     * Popular Movies Get 요청
     */

    public TmdbMovieListsResponse getPopularMovies(String language, Integer page, String region) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/movie/popular")
                        .queryParam("language", language)
                        .queryParam("page", page)
                        .queryParam("region", region)
                        .build())
                .retrieve()
                .bodyToMono(TmdbMovieListsResponse.class)
                .block();
    }
}
