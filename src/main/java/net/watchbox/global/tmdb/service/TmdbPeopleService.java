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
     * Details Get 요청
     * @person_id
     * 150242
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
}
