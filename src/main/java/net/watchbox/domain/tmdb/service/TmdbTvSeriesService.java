package net.watchbox.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.tmdb.response.tvseries.TmdbTvSeriesDetailsResponse;
import net.watchbox.global.properties.TmdbProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class TmdbTvSeriesService {
    private final WebClient.Builder webClientBuilder;
    private final TmdbProperties tmdbProperties;

    /**
     * Details Get 요청
     * @series_id
     */
    public TmdbTvSeriesDetailsResponse getTvSeriesDetails(Long seriesId) {
        WebClient webClient = webClientBuilder.baseUrl(tmdbProperties.getApi().getBaseUrl()).build();

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{seriesId}")
                        .queryParam("api_key", tmdbProperties.getApi().getKey())
                        .queryParam("language", "ko-KR")
                        .build(seriesId))
                .retrieve()
                .bodyToMono(TmdbTvSeriesDetailsResponse.class)
                .block();
    }
}
