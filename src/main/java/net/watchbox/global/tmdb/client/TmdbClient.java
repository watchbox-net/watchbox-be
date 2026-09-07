package net.watchbox.global.tmdb.client;

import lombok.extern.slf4j.Slf4j;
import net.watchbox.global.dto.response.exception.CustomException;
import net.watchbox.global.dto.response.exception.ErrorCode;
import net.watchbox.global.properties.TmdbProperties;
import net.watchbox.global.tmdb.util.TmdbUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class TmdbClient {
    private final TmdbProperties tmdbProperties;

    /**
     * WebClient 는 불변·스레드세이프라 <b>1회만 만들어 재사용</b>한다.
     *
     * <p>호출할 때마다 build() 하면 안 된다. {@code WebClient.Builder} 는 {@code filter()} 호출 시
     * 자기 자신을 변형(append)하고 반환하는데, 주입받는 빌더 빈이 싱글턴이라 호출할 때마다
     * retry 필터와 statusHandler 가 무한 누적된다.
     * → 요청마다 필터 체인이 길어져 시간이 갈수록 느려지고, retry 가 중첩돼 재시도가 곱연산으로 늘어난다.
     *
     * <p>{@code clone()} 은 주입받은 공유 빌더 자체를 오염시키지 않기 위한 것(다른 컴포넌트가 같은
     * 빌더 빈을 주입받을 수 있다).
     */
    private final WebClient webClient;

    public TmdbClient(WebClient.Builder webClientBuilder, TmdbProperties tmdbProperties) {
        this.tmdbProperties = tmdbProperties;
        this.webClient = webClientBuilder.clone()
                .baseUrl(tmdbProperties.getApi().getBaseUrl())
                // 일시적 오류(5xx, 429, 네트워크 예외) 자동 재시도 필터
                // - statusHandler 보다 먼저 실행되어, retryable 응답은 statusHandler 까지 안 도달
                .filter(retryFilter())
                // 4xx/5xx 응답을 CustomException 으로 변환 (재시도 후에도 실패하면 여기로)
                .defaultStatusHandler(HttpStatusCode::isError, TmdbClient::toCustomException)
                .build();
    }

    public WebClient baseWebClient() {
        return webClient;
    }

    /**
     * TMDB 응답 상태코드 → CustomException 변환.
     * - 404: 리소스 없음
     * - 그 외 4xx: 클라이언트 오류
     * - 5xx: 서버 오류 (재시도 소진 후 도달)
     */
    private static Mono<? extends Throwable> toCustomException(ClientResponse response) {
        int status = response.statusCode().value();
        log.warn("TMDB API error: status={}", status);
        if (status == 404) {
            return Mono.error(new CustomException(ErrorCode.TMDB_RESOURCE_NOT_FOUND));
        }
        if (response.statusCode().is4xxClientError()) {
            return Mono.error(new CustomException(ErrorCode.TMDB_CLIENT_ERROR));
        }
        return Mono.error(new CustomException(ErrorCode.TMDB_SERVER_ERROR));
    }

    /**
     * TMDB 호출 retry 필터.
     * - 5xx 또는 429 응답(Too Many Requests)은 일시적 오류로 간주, 마커 예외로 throw → retryWhen 이 catch
     * - 네트워크 예외(IOException, TimeoutException)는 그대로 retryWhen 이 catch
     * - 4xx 는 재시도 안 함 (요청 자체가 잘못된 거라 재시도 무의미)
     * - 지수 백오프 (500ms, 1s, 2s ...) 로 TMDB_MAX_RETRY 회 재시도
     * - 모두 실패하면 CustomException(TMDB_SERVER_ERROR) 로 변환
     */
    private ExchangeFilterFunction retryFilter() {
        return (request, next) -> next.exchange(request)
                .flatMap(response -> {
                    int status = response.statusCode().value();
                    if (status >= 500 || status == 429) {
                        // body 해제 후 retry 트리거용 마커 예외 발행
                        return response.releaseBody()
                                .then(Mono.error(new TmdbTransientException(status)));
                    }
                    return Mono.just(response);
                })
                .retryWhen(Retry
                        .backoff(TmdbUtils.TMDB_MAX_RETRY, Duration.ofMillis(500))
                        .filter(TmdbClient::isRetryable)
                        .doBeforeRetry(signal -> log.warn(
                                "TMDB retry attempt {}/{} - cause: {}",
                                signal.totalRetriesInARow() + 1,
                                TmdbUtils.TMDB_MAX_RETRY,
                                signal.failure().getMessage()))
                        // 재시도 소진 시 최종 예외 변환 (defaultStatusHandler 우회됨)
                        .onRetryExhaustedThrow((spec, signal) ->
                                new CustomException(ErrorCode.TMDB_SERVER_ERROR)));
    }

    /**
     * 재시도 대상 판단.
     * - TmdbTransientException: 5xx, 429 응답 (위에서 throw)
     * - IOException: 커넥션 끊김, DNS 실패 등 네트워크 오류
     * - TimeoutException: 응답 지연
     */
    private static boolean isRetryable(Throwable e) {
        return e instanceof TmdbTransientException
                || e instanceof IOException
                || e instanceof TimeoutException;
    }

    /**
     * retry 트리거용 내부 마커 예외 (외부 노출 X).
     * 5xx/429 응답을 retryWhen 이 catch 할 수 있도록 throwable 로 변환.
     */
    private static class TmdbTransientException extends RuntimeException {
        TmdbTransientException(int status) {
            super("TMDB transient error - status: " + status);
        }
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
