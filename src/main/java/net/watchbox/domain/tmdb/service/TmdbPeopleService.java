package net.watchbox.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import net.watchbox.domain.tmdb.response.people.TmdbPeopleDetailsResponse;
import net.watchbox.global.properties.TmdbProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class TmdbPeopleService {
    private final WebClient.Builder webClientBuilder;
    private final TmdbProperties tmdbProperties;

    /**
     * Details Get 요청
     * @person_id
     * 150242
     */
    public TmdbPeopleDetailsResponse getPeopleDetails(Long personId) {
        WebClient webClient = webClientBuilder.baseUrl(tmdbProperties.getApi().getBaseUrl()).build();

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/person/{personId}")
                        .queryParam("api_key", tmdbProperties.getApi().getKey())
                        .queryParam("language", "ko-KR")
                        .build(personId))
                .retrieve()
                .bodyToMono(TmdbPeopleDetailsResponse.class)
                .block();
    }
}
