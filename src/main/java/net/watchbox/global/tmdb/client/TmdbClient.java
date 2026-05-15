package net.watchbox.global.tmdb.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import net.watchbox.global.properties.TmdbProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbClient {
    private final WebClient.Builder webClientBuilder;
    private final TmdbProperties tmdbProperties;

    public WebClient baseWebClient() {
        return webClientBuilder
                .baseUrl(tmdbProperties.getApi().getBaseUrl())
                // 4xx/5xx 응답을 CustomException 으로 변환 (모든 TMDB 호출에 공통 적용)
                .defaultStatusHandler(HttpStatusCode::isError, response -> {
                    int status = response.statusCode().value();
                    log.warn("TMDB API error: status={}", status);
                    if (status == 404) {
                        return Mono.error(new CustomException(ErrorCode.TMDB_RESOURCE_NOT_FOUND));
                    }
                    if (response.statusCode().is4xxClientError()) {
                        return Mono.error(new CustomException(ErrorCode.TMDB_CLIENT_ERROR));
                    }
                    return Mono.error(new CustomException(ErrorCode.TMDB_SERVER_ERROR));
                })
                .build();
    }

    // 기본 공통 파라미터(api_key, language)를 추가한 UriBuilder 반환
    public UriBuilder addCommonParams(UriBuilder uriBuilder) {
        return uriBuilder
                .queryParam("api_key", tmdbProperties.getApi().getKey())
                .queryParam("language", "ko-KR")
                .queryParam("include_adult", tmdbProperties.getApi().isIncludeAdult());
    }

    public UriBuilder addCommonParams(UriBuilder uriBuilder, String language) {
        return uriBuilder
                .queryParam("api_key", tmdbProperties.getApi().getKey())
                .queryParam("language", language)
                .queryParam("include_adult", tmdbProperties.getApi().isIncludeAdult());
    }

    /**
     * 이미지 전체 URL 생성
     */
    public String buildImageUrl(String path) {
        return path != null ? tmdbProperties.getImageUrl() + path : null;
    }
}
