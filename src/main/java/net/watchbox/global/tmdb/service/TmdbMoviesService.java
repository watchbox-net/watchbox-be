package net.watchbox.global.tmdb.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import net.watchbox.global.tmdb.client.TmdbClient;
import net.watchbox.global.tmdb.response.common.TmdbWorkImagesResponse;
import net.watchbox.global.tmdb.response.movies.TmdbMoviesDetailsResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Observed
public class TmdbMoviesService {
    private final TmdbClient tmdbClient;

    /**
     * Details
     * https://api.themoviedb.org/3/movie/{movie_id}
     * DB 저장에 필요한 정보 요청
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

    /**
     * Details
     * https://api.themoviedb.org/3/movie/{movie_id}
     * 상세 페이지에 필요한 정보 요청
     */
    public TmdbMoviesDetailsResponse getMovieDetailsWithCVP(Long movieId) {
        return getMovieDetailsWithCVPMono(movieId).block();
    }

    public Mono<TmdbMoviesDetailsResponse> getMovieDetailsWithCVPMono(Long movieId) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .queryParam("append_to_response", "credits,videos,watch/providers")
                        .path("/movie/{movieId}")
                        .build(movieId))
                .retrieve()
                .bodyToMono(TmdbMoviesDetailsResponse.class);
    }

    /**
     * Images
     * https://api.themoviedb.org/3/movie/{movie_id}/images
     * language=null 영화 이미지 요청
     */
    public TmdbWorkImagesResponse getMovieImages(Long movieId) {
        return getMovieImagesMono(movieId).block();
    }

    public Mono<TmdbWorkImagesResponse> getMovieImagesMono(Long movieId) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder, "null")
                        .path("/movie/{movieId}/images")
                        .build(movieId))
                .retrieve()
                .bodyToMono(TmdbWorkImagesResponse.class);
    }

}
