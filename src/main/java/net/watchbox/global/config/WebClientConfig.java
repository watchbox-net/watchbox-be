package net.watchbox.global.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * 외부 API 호출용 WebClient 설정. 현재 사용처는 {@code TmdbClient} 하나다.
 *
 * <p>기본값(설정 없음)일 때 확인된 문제 두 가지를 잡는다.
 *
 * <p><b>1) 커넥션 재수립 비용</b> — 캐시 미스 시 TMDB 호출 8건이 동시에 나가는데 HTTP/1.1 은
 * 요청당 커넥션이 필요해 TLS 핸드셰이크가 8번 발생한다. 실측에서 같은 요청 안의 TMDB 호출이
 * 250~270ms(커넥션 재사용)와 820~900ms(신규 수립)로 갈렸다.
 * → HTTP/2 로 협상하면 커넥션 하나에 8건을 멀티플렉싱해 핸드셰이크가 1회로 준다.
 *
 * <p><b>2) 타임아웃 부재</b> — 응답 타임아웃이 없어 TMDB 가 응답하지 않으면 무한정 대기했다.
 * {@code TmdbClient} 재시도 필터의 {@code TimeoutException} 분기도 이 설정이 없어 죽은 코드였다.
 *
 * <p>캐싱 도입 후 TMDB 호출이 드물어져 커넥션이 서버(Cloudflare) 쪽 keep-alive 타임아웃으로
 * 끊긴 상태가 오래 지속된다. {@code maxIdleTime} 은 그 끊긴 커넥션을 재사용 시도하다 실패하는 왕복을
 * 줄여줄 뿐, 커넥션을 살려두지는 못한다. 상시 유지는 주기적 호출(캐시 워밍)의 몫이다.
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    /** 단일 요청 응답 대기 한도. TMDB 정상 응답은 250~900ms 수준이라 5초면 충분히 여유가 있다. */
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(5);

    /** TCP 연결 수립 한도. */
    private static final int CONNECT_TIMEOUT_MILLIS = 3_000;

    /** 서버가 먼저 끊기 전에 우리가 정리한다. Cloudflare keep-alive 가 보통 수십 초. */
    private static final Duration MAX_IDLE_TIME = Duration.ofSeconds(20);

    @Bean
    public WebClient.Builder webClientBuilder() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("tmdb")
                .maxConnections(50)
                .maxIdleTime(MAX_IDLE_TIME)
                .maxLifeTime(Duration.ofMinutes(10))
                .evictInBackground(Duration.ofSeconds(30))
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                // ALPN 협상. h2 가 안 되면 HTTP/1.1 로 자동 폴백한다.
                .protocol(HttpProtocol.HTTP11, HttpProtocol.H2)
                .responseTimeout(RESPONSE_TIMEOUT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024)); // 16MB
    }
}
