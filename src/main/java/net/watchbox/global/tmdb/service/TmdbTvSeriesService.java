package net.watchbox.global.tmdb.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import net.watchbox.global.tmdb.client.TmdbClient;
import net.watchbox.global.tmdb.response.common.TmdbWorkImagesResponse;
import net.watchbox.global.tmdb.response.tvseries.TmdbTvSeriesDetailsResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Observed
public class TmdbTvSeriesService {
    private final TmdbClient tmdbClient;

    /**
     * Details
     * https://api.themoviedb.org/3/tv/{series_id}
     * 상세 페이지에 필요한 정보들 요청
     */
    public TmdbTvSeriesDetailsResponse getTvSeriesDetails(Long seriesId) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .queryParam("append_to_response", "credits,videos,watch/providers")
                        .path("/tv/{seriesId}")
                        .build(seriesId))
                .retrieve()
                .bodyToMono(TmdbTvSeriesDetailsResponse.class)
                .block();
    }

    /**
     * Images
     * https://api.themoviedb.org/3/tv/{series_id}/images
     * language=null TvSeries 이미지 요청
     */
    public TmdbWorkImagesResponse getTvSeriesDetailsWithImages(Long seriesId) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder, "null")
                        .path("/tv/{seriesId}/images")
                        .build(seriesId))
                .retrieve()
                .bodyToMono(TmdbWorkImagesResponse.class)
                .block();
    }
}
