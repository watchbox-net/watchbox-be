package net.watchbox.global.tmdb.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.watchbox.global.properties.TmdbProperties;
import net.watchbox.global.tmdb.response.movielists.TmdbMovieListsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * read-through 캐시의 핵심 계약 검증.
 * 특히 <b>hit 이면 TMDB 를 부르지 않는다</b>는 것과, <b>캐시가 요청을 실패시키지 않는다</b>는 것.
 */
class TmdbResponseCacheTest {

    private static final String KEY = "tmdb:movie:popular:1";
    private static final String TMDB_JSON = """
            {"page":1,"results":[],"total_pages":10,"total_results":200}""";

    private ReactiveStringRedisTemplate redis;
    private ReactiveValueOperations<String, String> valueOps;
    private ObjectMapper objectMapper;
    private TmdbProperties properties;
    private TmdbResponseCache cache;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        redis = mock(ReactiveStringRedisTemplate.class);
        valueOps = mock(ReactiveValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);

        objectMapper = new ObjectMapper();

        properties = new TmdbProperties();
        properties.setCache(new TmdbProperties.Cache());

        cache = new TmdbResponseCache(redis, objectMapper, properties);
    }

    private TmdbMovieListsResponse tmdbResponse() throws Exception {
        return objectMapper.readValue(TMDB_JSON, TmdbMovieListsResponse.class);
    }

    @Test
    @DisplayName("캐시 hit 이면 TMDB 를 호출하지 않는다")
    void hit이면_TMDB_호출안함() {
        when(valueOps.get(KEY)).thenReturn(Mono.just(TMDB_JSON));
        AtomicInteger tmdbCalls = new AtomicInteger();

        TmdbMovieListsResponse result = cache.readThrough(
                KEY, TmdbResponseCache.Ttl.STABLE, TmdbMovieListsResponse.class,
                () -> {
                    tmdbCalls.incrementAndGet();
                    return Mono.error(new AssertionError("hit 인데 TMDB 를 불렀다"));
                }
        ).block();

        assertThat(tmdbCalls).hasValue(0);
        assertThat(result).isNotNull();
        assertThat(result.getTotalResults()).isEqualTo(200); // 역직렬화 왕복 확인
        verify(valueOps, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("캐시 miss 이면 TMDB 를 호출하고 TTL 과 함께 저장한다")
    void miss면_TMDB_호출하고_저장() throws Exception {
        when(valueOps.get(KEY)).thenReturn(Mono.empty());
        when(valueOps.set(anyString(), anyString(), any(Duration.class))).thenReturn(Mono.just(true));
        AtomicInteger tmdbCalls = new AtomicInteger();

        TmdbMovieListsResponse loaded = tmdbResponse();
        TmdbMovieListsResponse result = cache.readThrough(
                KEY, TmdbResponseCache.Ttl.TRENDING, TmdbMovieListsResponse.class,
                () -> {
                    tmdbCalls.incrementAndGet();
                    return Mono.just(loaded);
                }
        ).block();

        assertThat(tmdbCalls).hasValue(1);
        assertThat(result).isSameAs(loaded);
        // TRENDING 등급의 TTL(기본 15분)로 저장되어야 한다
        verify(valueOps).set(eq(KEY), anyString(), eq(Duration.ofMinutes(15)));
    }

    @Test
    @DisplayName("Redis 조회가 실패해도 TMDB 호출로 폴백한다 (캐시는 요청을 실패시키지 않는다)")
    void redis장애면_폴백() throws Exception {
        when(valueOps.get(KEY)).thenReturn(Mono.error(new RuntimeException("redis down")));
        when(valueOps.set(anyString(), anyString(), any(Duration.class)))
                .thenReturn(Mono.error(new RuntimeException("redis down")));

        TmdbMovieListsResponse loaded = tmdbResponse();
        TmdbMovieListsResponse result = cache.readThrough(
                KEY, TmdbResponseCache.Ttl.STABLE, TmdbMovieListsResponse.class,
                () -> Mono.just(loaded)
        ).block();

        assertThat(result).isSameAs(loaded); // 저장까지 실패해도 응답은 정상
    }

    @Test
    @DisplayName("깨진 캐시 값은 miss 로 간주하고 TMDB 를 다시 부른다")
    void 깨진캐시값이면_miss처리() throws Exception {
        when(valueOps.get(KEY)).thenReturn(Mono.just("{ 깨진 JSON"));
        when(valueOps.set(anyString(), anyString(), any(Duration.class))).thenReturn(Mono.just(true));

        TmdbMovieListsResponse loaded = tmdbResponse();
        TmdbMovieListsResponse result = cache.readThrough(
                KEY, TmdbResponseCache.Ttl.STABLE, TmdbMovieListsResponse.class,
                () -> Mono.just(loaded)
        ).block();

        assertThat(result).isSameAs(loaded);
    }

    @Test
    @DisplayName("enabled=false 면 Redis 를 아예 건드리지 않는다 (A/B 측정용 토글)")
    void 토글끄면_캐시우회() throws Exception {
        properties.getCache().setEnabled(false);

        TmdbMovieListsResponse loaded = tmdbResponse();
        TmdbMovieListsResponse result = cache.readThrough(
                KEY, TmdbResponseCache.Ttl.STABLE, TmdbMovieListsResponse.class,
                () -> Mono.just(loaded)
        ).block();

        assertThat(result).isSameAs(loaded);
        verify(valueOps, never()).get(anyString());
        verify(valueOps, never()).set(anyString(), anyString(), any(Duration.class));
    }
}
