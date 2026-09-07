package net.watchbox.global.tmdb.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.global.properties.TmdbProperties;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * TMDB 응답 read-through 캐시.
 *
 * <p><b>왜 캐시가 가능한가</b>: TMDB 목록은 사용자와 무관하다. 응답을 결정하는 건
 * (목록 종류, page, timeWindow) 뿐이고, 개인화(시청기록)는 이 계층 <b>밖</b>에서 얹는다.
 * 그래서 캐시에 사용자 데이터가 섞이지 않는다.
 *
 * <p><b>왜 매핑 결과가 아니라 TMDB 원본 응답을 캐시하나</b>: 매핑 후 DTO({@code ContentItem})는
 * {@code @Builder} 만 있고 Jackson 역직렬화 경로가 없어 캐시에서 복원할 수 없다. 반면 TMDB 응답 DTO 는
 * 애초에 TMDB JSON 을 역직렬화해 만든 것이라 왕복이 보장된다. 아끼려는 비용도 HTTP 왕복이지
 * 매핑(아이템 20개)이 아니다.
 *
 * <p><b>캐시는 절대 요청을 실패시키지 않는다</b>: Redis 장애·직렬화 실패는 모두 miss 로 간주하고
 * TMDB 를 직접 호출한다. 캐시는 가속 장치이지 의존 대상이 아니다.
 *
 * <p><b>알려진 한계 — cache stampede</b>: 인기 키의 TTL 이 만료되는 순간 동시 요청이 모두 miss 가 되어
 * TMDB 로 몰릴 수 있다. 현재 트래픽 규모에서는 감수한다. 필요해지면 스케줄 워밍이나 키 단위 락으로 대응.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbResponseCache {

    /**
     * 관측 이름. Micrometer Observation 은 <b>span 과 metric 을 동시에</b> 만든다.
     * <ul>
     *   <li>트레이스: {@code tmdb.cache} span + {@code cache.result=hit|miss} 태그
     *       → waterfall 에서 캐시 적중 여부가 바로 보인다</li>
     *   <li>메트릭: 같은 태그가 붙은 타이머 → Grafana 에서 <b>실제 hit rate</b> 집계 가능
     *       (캐시 워밍 도입 근거를 추정이 아니라 실측으로 세우기 위함)</li>
     * </ul>
     */
    private static final String OBSERVATION_NAME = "tmdb.cache";

    private final ReactiveStringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final TmdbProperties tmdbProperties;
    private final ObservationRegistry observationRegistry;

    /** TTL 등급. 실제 값은 {@code tmdb.cache.*} 설정에서 온다. */
    public enum Ttl {
        /** trending — 자주 바뀐다 */
        TRENDING,
        /** now-playing / on-the-air */
        NOW_SHOWING,
        /** popular / top-rated — 거의 고정 */
        STABLE
    }

    /**
     * 캐시에 있으면 그대로 돌려주고, 없으면 {@code loader} 를 호출한 뒤 저장한다.
     *
     * <p>관측은 {@code Mono.defer} 안에서 시작한다. 조립 시점이 아니라 <b>구독 시점</b>에 시작해야
     * span 이 실제 실행 구간을 재고, 8개 섹션이 동시에 진행되는 모습이 waterfall 에 겹쳐 보인다.
     * (조립 시점에 재면 μs 짜리 span 이 순차로 찍혀 병렬이 아닌 것처럼 오해를 부른다)
     *
     * @param key    캐시 키. 응답을 결정하는 값(목록 종류·page·timeWindow)을 모두 담아야 한다
     * @param loader 캐시 미스일 때 실행할 TMDB 호출
     */
    public <T> Mono<T> readThrough(String key, Ttl ttl, Class<T> type, Supplier<Mono<T>> loader) {
        if (!tmdbProperties.getCache().isEnabled()) {
            return loader.get();
        }
        return Mono.defer(() -> {
            Observation observation = Observation.createNotStarted(OBSERVATION_NAME, observationRegistry)
                    .lowCardinalityKeyValue("cache.name", "tmdb")
                    // 미스 경로로 안 가고 에러가 나도 태그가 비지 않도록 기본값을 먼저 넣는다
                    .lowCardinalityKeyValue("cache.result", "miss")
                    .highCardinalityKeyValue("cache.key", key)
                    .start();

            return lookup(key, type)
                    .doOnNext(hit -> observation.lowCardinalityKeyValue("cache.result", "hit"))
                    .switchIfEmpty(Mono.defer(() -> loadAndStore(key, ttl, loader)))
                    .doOnError(observation::error)
                    .doFinally(signal -> observation.stop())
                    // 이 관측을 "현재 관측"으로 만들어 하위 계측(Redis·WebClient)이 부모로 잡게 한다.
                    // 넣지 않으면 Redis/TMDB span 이 이 span 이 아니라 상위(facade)에 붙어 평평해지고,
                    // 어떤 TMDB 호출이 어느 캐시 키의 것인지 트레이스에서 구분할 수 없다.
                    // (contextWrite 는 상류에 적용되므로 lookup·loadAndStore 가 대상이 된다)
                    .contextWrite(ctx -> ctx.put(ObservationThreadLocalAccessor.KEY, observation));
        });
    }

    /** Redis 조회. 장애·깨진 값은 모두 빈 결과(=miss)로 흡수한다. */
    private <T> Mono<T> lookup(String key, Class<T> type) {
        return redis.opsForValue().get(key)
                .onErrorResume(e -> {
                    log.warn("tmdb cache read failed, bypassing cache - key={}, cause={}", key, e.getMessage());
                    return Mono.empty();
                })
                .flatMap(cached -> deserialize(cached, type, key));
    }

    private <T> Mono<T> deserialize(String json, Class<T> type, String key) {
        try {
            return Mono.just(objectMapper.readValue(json, type));
        } catch (Exception e) {
            // 캐시 포맷이 바뀐 경우 등. miss 로 간주해 원본을 다시 부른다.
            log.warn("tmdb cache deserialize failed, treating as miss - key={}, cause={}", key, e.getMessage());
            return Mono.empty();
        }
    }

    private <T> Mono<T> loadAndStore(String key, Ttl ttl, Supplier<Mono<T>> loader) {
        return loader.get()
                .flatMap(value -> store(key, value, ttl).thenReturn(value));
    }

    private <T> Mono<Boolean> store(String key, T value, Ttl ttl) {
        String json;
        try {
            json = objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("tmdb cache serialize failed, skipping store - key={}, cause={}", key, e.getMessage());
            return Mono.just(false);
        }
        return redis.opsForValue().set(key, json, durationOf(ttl))
                .onErrorResume(e -> {
                    log.warn("tmdb cache write failed - key={}, cause={}", key, e.getMessage());
                    return Mono.just(false);
                });
    }

    private Duration durationOf(Ttl ttl) {
        TmdbProperties.Cache cache = tmdbProperties.getCache();
        return switch (ttl) {
            case TRENDING -> cache.getTrendingTtl();
            case NOW_SHOWING -> cache.getNowShowingTtl();
            case STABLE -> cache.getStableTtl();
        };
    }
}
