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

        /**
         * 기동 직후 홈 캐시를 미리 채울지 여부.
         *
         * <p>배포 직후 첫 요청은 캐시가 hit 이어도 1.4~1.9초가 걸린다. 원인이 캐시가 아니라
         * JIT·클래스 로딩·커넥션 수립 같은 <b>앱 콜드 스타트</b>이기 때문이다.
         * 그 비용을 실사용자가 아니라 기동 시의 합성 요청이 대신 내게 한다.
         */
        private boolean warmupOnStartup = true;

        /** TTL 만료 전에 캐시를 주기적으로 선제 갱신할지 여부. */
        private boolean warmupScheduled = true;

        /**
         * 주기 워밍 간격. <b>반드시 가장 짧은 TTL 보다 작아야</b> 만료 전에 갱신된다.
         *
         * <p>이 관계에서 TTL 의 역할이 바뀐다 — 평시 만료는 워밍이 막고, TTL 은 워밍이 멈췄을 때
         * 낡은 데이터가 영원히 남지 않게 하는 <b>안전망</b>이 된다.
         */
        private Duration warmupInterval = Duration.ofMinutes(90);

        /**
         * trending TTL.
         *
         * <p>홈은 {@code timeWindow=week} 로 조회하는데 TMDB 의 주간 트렌딩은 그렇게 자주 바뀌지 않는다.
         * 짧은 TTL 은 신선도에 도움이 안 되면서 hit rate 만 떨어뜨린다 —
         * hit rate 는 대략 {@code TTL / 평균 방문간격} 이라, 저트래픽에서 TTL 15분은
         * 대부분의 방문이 miss 가 된다는 뜻이었다.
         */
        private Duration trendingTtl = Duration.ofHours(2);

        /** now-playing / on-the-air — 하루 단위로 바뀐다 */
        private Duration nowShowingTtl = Duration.ofHours(2);

        /** popular / top-rated — 거의 고정 */
        private Duration stableTtl = Duration.ofHours(12);
    }
}