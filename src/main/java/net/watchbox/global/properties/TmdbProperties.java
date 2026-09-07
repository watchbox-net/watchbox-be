package net.watchbox.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Setter
@Getter
@Component
@ConfigurationProperties("tmdb")
public class TmdbProperties {
    private String imageUrl;
    private Api api;
    private Cache cache = new Cache();

    @Setter
    @Getter
    public static class Api {
        private String key;
        private String baseUrl;
        private boolean includeAdult;
    }

    /**
     * TMDB 응답 캐시 설정.
     *
     * <p>TTL 은 목록의 변동성에 따라 차등한다. TMDB 데이터는 우리가 바꿀 수 없으니
     * 명시적 무효화(evict) 없이 TTL 만료만으로 갱신한다.
     *
     * <p>{@code enabled} 는 캐시 on/off 토글이다. 같은 빌드로 캐시 유무를 A/B 측정하기 위한 것으로,
     * 빌드 차이가 성능 비교에 섞이지 않게 한다.
     */
    @Setter
    @Getter
    public static class Cache {
        private boolean enabled = true;

        /** trending — 자주 바뀐다 */
        private Duration trendingTtl = Duration.ofMinutes(15);

        /** now-playing / on-the-air — 하루 단위로 바뀐다 */
        private Duration nowShowingTtl = Duration.ofHours(2);

        /** popular / top-rated — 거의 고정 */
        private Duration stableTtl = Duration.ofHours(12);
    }
}