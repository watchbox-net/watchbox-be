package net.watchbox.global.tmdb.client;

import net.watchbox.global.properties.TmdbProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WebClient.Builder 는 filter() 호출 시 자기 자신을 변형하고 반환한다.
 * 빌더 빈이 싱글턴이므로, baseWebClient() 안에서 매번 build() 하면 필터가 무한 누적되어
 * 시간이 갈수록 느려지고 retry 가 중첩된다. 회귀 방지용 테스트.
 */
class TmdbClientBuilderLeakTest {

    private static TmdbProperties props() {
        TmdbProperties props = new TmdbProperties();
        TmdbProperties.Api api = new TmdbProperties.Api();
        api.setBaseUrl("https://api.themoviedb.org/3");
        api.setKey("dummy");
        props.setApi(api);
        return props;
    }

    private static int filterCountOf(WebClient.Builder builder) {
        List<Integer> counts = new ArrayList<>();
        builder.filters(f -> counts.add(f.size()));
        return counts.get(0);
    }

    @Test
    @DisplayName("baseWebClient() 를 반복 호출해도 주입받은 공유 빌더가 오염되지 않는다")
    void baseWebClient_반복호출해도_공유빌더_오염없음() {
        WebClient.Builder sharedBuilder = WebClient.builder();
        TmdbClient client = new TmdbClient(sharedBuilder, props());

        for (int i = 0; i < 100; i++) {
            client.baseWebClient();
        }

        assertThat(filterCountOf(sharedBuilder)).isZero();
    }

    @Test
    @DisplayName("baseWebClient() 는 매번 새로 만들지 않고 같은 인스턴스를 재사용한다")
    void baseWebClient_동일인스턴스_재사용() {
        TmdbClient client = new TmdbClient(WebClient.builder(), props());

        assertThat(client.baseWebClient()).isSameAs(client.baseWebClient());
    }
}
