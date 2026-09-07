package net.watchbox.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 커넥션 풀·HTTP/2·타임아웃 설정이 조립 단계에서 터지지 않는지 확인한다.
 * (전체 컨텍스트 테스트는 환경변수 문제로 이 빈까지 도달하지 못한다)
 */
class WebClientConfigTest {

    @Test
    @DisplayName("WebClient 빌더가 커넥션 풀·H2 설정과 함께 생성된다")
    void 빌더가_정상_생성된다() {
        WebClient.Builder builder = new WebClientConfig().webClientBuilder();

        assertThat(builder).isNotNull();
        assertThat(builder.build()).isNotNull();
    }

    @Test
    @DisplayName("TmdbClient 처럼 clone 해서 써도 문제없다")
    void clone_후_사용가능() {
        WebClient.Builder builder = new WebClientConfig().webClientBuilder();

        WebClient client = builder.clone()
                .baseUrl("https://api.themoviedb.org/3")
                .build();

        assertThat(client).isNotNull();
    }
}
