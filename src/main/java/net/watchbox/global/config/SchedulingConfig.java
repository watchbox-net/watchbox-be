package net.watchbox.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {@code @Scheduled} 활성화. (SSE 하트비트, 홈 캐시 워밍, outbox 릴레이)
 *
 * <p><b>예전의 {@code AsyncConfig} 를 대체한다.</b> 거기에는 알림·메일 전용 스레드풀이 있었는데,
 * 알림 발행이 outbox 를 거치면서 {@code @Async} 리스너가 사라져 함께 없앴다.
 *
 * <p>그 풀들은 <b>둘 중 하나를 고를 수밖에 없는 구조</b>였다 — 큐가 차면 버리거나
 * ({@code DiscardPolicy}, 유실) 호출 스레드에서 실행하거나({@code CallerRunsPolicy}, 지연).
 * {@code AFTER_COMMIT} 콜백은 HTTP 요청 스레드에서 돌기 때문에 후자는 곧 <b>사용자 요청이
 * SMTP 를 기다린다</b>는 뜻이었다. 메모리 큐로는 유실과 지연 중 하나를 반드시 감수해야 한다.
 *
 * <p>outbox 는 넘치는 작업을 <b>디스크에 쌓으므로</b> 버릴 이유도, 요청 스레드를 잡을 이유도 없다.
 *
 * <p><b>주의</b>: 기본 {@code TaskScheduler} 는 스레드가 1개다. {@code @Scheduled} 안에서
 * 블로킹하면 다른 스케줄 작업이 밀리므로, 오래 걸리는 일은 가상 스레드로 넘긴다
 * ({@code HomeCacheWarmer}, {@code OutboxRelay} 가 그렇게 한다).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
