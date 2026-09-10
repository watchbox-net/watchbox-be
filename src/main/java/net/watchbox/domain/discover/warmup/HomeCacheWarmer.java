package net.watchbox.domain.discover.warmup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.global.properties.TmdbProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 홈 캐시를 사용자 요청과 무관하게 미리·계속 채워둔다.
 *
 * <p><b>목표는 사용자가 캐시 미스를 아예 겪지 않게 하는 것이다.</b> read-through 캐시만으로는
 * 두 구멍이 남는다.
 * <ul>
 *   <li><b>기동 직후</b> — 배포 후 첫 요청은 캐시가 hit 이어도 1.4~1.9초가 걸렸다(실측).
 *       원인이 캐시가 아니라 JIT·클래스 로딩·커넥션 수립 같은 <b>앱 콜드 스타트</b>였다.</li>
 *   <li><b>TTL 만료 후</b> — 만료 뒤 첫 방문자가 miss(~900ms)를 대신 맞는다.
 *       TTL 을 늘려 빈도를 줄일 수는 있어도 없앨 수는 없다.</li>
 * </ul>
 * 그래서 기동 시 1회 + TTL 만료 전 주기적으로 갱신해, <b>그 비용을 실사용자가 아니라
 * 백그라운드 작업이 대신 내게</b> 한다.
 *
 * <p><b>왜 강제 갱신인가</b>: 섹션 조회를 그냥 주기 호출하면 캐시가 hit 이라
 * TMDB 를 부르지 않아 <b>갱신이 일어나지 않는다</b>. 조회를 건너뛰는 경로가 따로 필요하다.
 * ({@link HomeTmdbSectionLoader#refreshCache})
 *
 * <p><b>기동·스케줄러를 막지 않는다</b>: 가상 스레드에서 돌고, 실패해도 로그만 남긴다.
 * TMDB 장애로 애플리케이션이 못 뜨거나 다른 {@code @Scheduled} 작업(SSE 하트비트)이
 * 밀리는 일이 없어야 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HomeCacheWarmer {

    /** 홈이 실제로 사용하는 값과 같아야 캐시 키가 맞는다. */
    private static final String TIME_WINDOW = "week";
    private static final int PAGE = 1;

    private final HomeTmdbSectionLoader homeTmdbSectionLoader;
    private final TmdbProperties tmdbProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!tmdbProperties.getCache().isWarmupOnStartup()) {
            log.info("home cache warm-up skipped (tmdb.cache.warmup-on-startup=false)");
            return;
        }
        runInBackground("startup");
    }

    /**
     * TTL 만료 전 선제 갱신.
     *
     * <p><b>간격은 가장 짧은 TTL 보다 작아야 한다.</b> {@code fixedDelay} 는 직전 실행
     * <b>완료</b> 뒤부터 세므로 실제 간격은 설정값보다 조금 길다 — 같게 두면 갱신이 만료보다
     * 늦어져 매 주기 빈 구간이 생긴다. 현재는 1h 간격 / 2h TTL 로 여유를 1시간 둔다.
     *
     * <p>{@code initialDelay} 로 첫 실행을 미루는 이유는 기동 워밍과 겹쳐 TMDB 를 두 번 치지 않기 위함.
     */
    @Scheduled(
            initialDelayString = "${tmdb.cache.warmup-interval}",
            fixedDelayString = "${tmdb.cache.warmup-interval}"
    )
    public void refreshPeriodically() {
        if (!tmdbProperties.getCache().isWarmupScheduled()) {
            return;
        }
        runInBackground("scheduled");
    }

    /**
     * 기본 {@code TaskScheduler} 는 스레드가 1개라, 여기서 블로킹하면 SSE 하트비트 같은 다른
     * {@code @Scheduled} 작업이 밀린다. 1회성 작업이라 상설 스레드풀도 두지 않는다.
     * (이름 없는 {@code @Async} 는 SimpleAsyncTaskExecutor 를 쓰게 되어 AsyncConfig 규약 위반)
     */
    private void runInBackground(String trigger) {
        Thread.ofVirtual().name("tmdb-cache-warmup-" + trigger).start(() -> warmUp(trigger));
    }

    /** 개인화 없이 캐시 대상 계층만 강제 갱신한다. */
    void warmUp(String trigger) {
        long startedAt = System.currentTimeMillis();
        try {
            homeTmdbSectionLoader.refreshCache(TIME_WINDOW, PAGE);
            log.info("home cache warm-up done - trigger={}, took={}ms",
                    trigger, System.currentTimeMillis() - startedAt);
        } catch (Exception e) {
            // 실패해도 손해가 없다. 기존 캐시는 TTL 까지 살아있고, 다음 주기에 다시 시도한다.
            log.warn("home cache warm-up failed, keeping existing cache - trigger={}, cause={}",
                    trigger, e.getMessage());
        }
    }
}
