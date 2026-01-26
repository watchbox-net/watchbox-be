package net.watchbox.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.tmdb.client.TmdbClient;
import net.watchbox.domain.tmdb.response.tvseries.TmdbTvSeriesDetailsResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TmdbTvSeriesService {
    private final TmdbClient tmdbClient;

    /**
     * Details Get 요청
     * @series_id
     */
    public TmdbTvSeriesDetailsResponse getTvSeriesDetails(Long seriesId) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/tv/{seriesId}")
                        .build(seriesId))
                .retrieve()
                .bodyToMono(TmdbTvSeriesDetailsResponse.class)
                .block();
    }
}
