package net.watchbox.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.tmdb.client.TmdbClient;
import net.watchbox.domain.tmdb.response.tvserieslists.TmdbTvSeriesListsResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TmdbTvSeriesListsService {
    private final TmdbClient tmdbClient;

    /**
     * Popular List Get 요청
     */
    public TmdbTvSeriesListsResponse getPopularTvSeriesLists(Integer page) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/tv/popular")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(TmdbTvSeriesListsResponse.class)
                .block();
    }
}
