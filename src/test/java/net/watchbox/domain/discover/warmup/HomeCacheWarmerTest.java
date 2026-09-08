package net.watchbox.domain.discover.warmup;

import net.watchbox.global.properties.TmdbProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class HomeCacheWarmerTest {

    private HomeTmdbSectionLoader homeTmdbSectionLoader;
    private TmdbProperties properties;
    private HomeCacheWarmer warmer;

    @BeforeEach
    void setUp() {
        homeTmdbSectionLoader = mock(HomeTmdbSectionLoader.class);
        properties = new TmdbProperties();
        properties.setCache(new TmdbProperties.Cache());
        warmer = new HomeCacheWarmer(homeTmdbSectionLoader, properties);
    }

    @Test
    @DisplayName("조회가 아니라 강제 갱신을 호출한다 (getHome 은 hit 이라 갱신이 안 된다)")
    void 강제갱신_경로를_호출() {
        warmer.warmUp("test");

        // 캐시 키가 맞으려면 timeWindow·page 가 홈과 같아야 한다
        verify(homeTmdbSectionLoader).refreshCache(eq("week"), eq(1));
        // load 를 부르면 캐시 hit 으로 끝나 TMDB 를 다시 받지 않는다 — 워밍의 핵심 함정
        verify(homeTmdbSectionLoader, never()).load(anyString(), anyInt());
    }

    @Test
    @DisplayName("워밍업이 실패해도 예외를 전파하지 않는다 (기동·스케줄러를 막으면 안 된다)")
    void 실패해도_예외_전파안함() {
        doThrow(new RuntimeException("TMDB down"))
                .when(homeTmdbSectionLoader).refreshCache(anyString(), anyInt());

        assertThatCode(() -> warmer.warmUp("test")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("기동 워밍 토글이 꺼져 있으면 아무것도 하지 않는다")
    void 기동토글끄면_동작안함() {
        properties.getCache().setWarmupOnStartup(false);

        warmer.onApplicationReady();

        verify(homeTmdbSectionLoader, never()).refreshCache(anyString(), anyInt());
    }

    @Test
    @DisplayName("주기 워밍 토글이 꺼져 있으면 아무것도 하지 않는다")
    void 주기토글끄면_동작안함() {
        properties.getCache().setWarmupScheduled(false);

        warmer.refreshPeriodically();

        verify(homeTmdbSectionLoader, never()).refreshCache(anyString(), anyInt());
    }
}
