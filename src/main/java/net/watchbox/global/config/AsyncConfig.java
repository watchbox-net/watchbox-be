package net.watchbox.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 처리용 스레드풀 설정.
 *
 * <p>{@code @Async} 사용 시 메서드별로 적절한 executor 를 지정 — bean 이름 미지정 시 SimpleAsyncTaskExecutor
 * 가 사용되어 매 호출마다 새 스레드를 만드므로 절대 사용하지 않을 것.
 *
 * <h3>Executors</h3>
 * <ul>
 *   <li>{@code notificationExecutor} — Notification 저장 + SSE push 전용.
 *       발행 도메인 트랜잭션 커밋 후 (AFTER_COMMIT) 비동기로 실행.
 *       큐 가득 차면 CallerRunsPolicy 로 발행 스레드가 직접 처리 — 유실 방지.</li>
 * </ul>
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean("notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("noti-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);
        executor.initialize();
        return executor;
    }
}
