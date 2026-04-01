package net.watchbox.global.tmdb.client;

import lombok.RequiredArgsConstructor;
import net.watchbox.global.properties.TmdbProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

@Component
@RequiredArgsConstructor
public class TmdbClient {
    private final WebClient.Builder webClientBuilder;
    private final TmdbProperties tmdbProperties;

    public WebClient baseWebClient() {
        return webClientBuilder
                .baseUrl(tmdbProperties.getApi().getBaseUrl())
                .build();
    }

    // 기본 공통 파라미터(api_key, language)를 추가한 UriBuilder 반환
    public UriBuilder addCommonParams(UriBuilder uriBuilder) {
        return uriBuilder
                .queryParam("api_key", tmdbProperties.getApi().getKey())
                .queryParam("language", "ko-KR");
    }

    /**
     * 이미지 전체 URL 생성
     */
    public String buildImageUrl(String path) {
        return path != null ? tmdbProperties.getImageUrl() + path : null;
    }
}
