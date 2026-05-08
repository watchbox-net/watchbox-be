package net.watchbox.global.tmdb.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import net.watchbox.global.tmdb.client.TmdbClient;
import net.watchbox.global.tmdb.response.people.TmdbPersonDetailsResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Observed
public class TmdbPeopleService {
    private final TmdbClient tmdbClient;

    /**
     * Details
     * https://api.themoviedb.org/3/person/{person_id}
     * DB 저장에 필요한 정보 요청
     */
    public TmdbPersonDetailsResponse getPeopleDetails(Long personId) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .path("/person/{personId}")
                        .build(personId))
                .retrieve()
                .bodyToMono(TmdbPersonDetailsResponse.class)
                .block();
    }

    /**
     * Details
     * https://api.themoviedb.org/3/person/{person_id}
     * 상세 페이지에 필요한 정보 요청
     */
    public TmdbPersonDetailsResponse getPeopleDetailsWithCI(Long personId) {
        return tmdbClient.baseWebClient()
                .get()
                .uri(uriBuilder -> tmdbClient.addCommonParams(uriBuilder)
                        .queryParam("append_to_response", "combined_credits,images")
                        .path("/person/{personId}")
                        .build(personId))
                .retrieve()
                .bodyToMono(TmdbPersonDetailsResponse.class)
                .block();
    }
}
