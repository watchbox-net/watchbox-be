package net.watchbox.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
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
 *   <li>{@code mailExecutor} — 메일 발송 전용. SSE 와 <b>풀을 나누는 이유는 격리</b>다.
 *       SMTP 왕복은 수백 ms~수 초라 같은 풀을 쓰면 메일이 느려질 때 SSE 푸시까지 함께 막힌다.</li>
 * </ul>
 */
@Slf4j
@EnableAsync
@EnableScheduling // SSE heartbeat 등 @Scheduled 작업용
@Configuration
public class AsyncConfig {

    public static final String MAIL_EXECUTOR = "mailExecutor";

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

    /**
     * 메일 발송 전용 풀. SMTP 왕복이 길어 스레드가 오래 잡히므로 코어를 작게 두고 큐로 흡수한다.
     *
     * <p>거부 정책은 CallerRunsPolicy 다 — <b>초대 메일을 버리면 상대가 초대받은 사실조차 모른다.</b>
     * 큐가 찰 정도면 이미 비정상이므로 그때는 느려지더라도 호출 스레드에서 마저 보낸다.
     *
     * <p><b>다만 그 호출 스레드는 사용자 요청 스레드다.</b> {@code AFTER_COMMIT} 콜백은 커밋한 스레드
     * (= HTTP 요청 스레드)에서 돌고, 거기서 {@code @Async} 가 이 풀로 넘긴다. 넘기지 못하면 그 자리에서
     * 실행되므로 <b>초대 API 응답이 SMTP 왕복만큼 늦어진다.</b> 즉 이 정책은 "유실이냐 지연이냐" 중
     * 지연을 고른 것이고, 둘 다 피하려면 큐가 아니라 <b>영속 큐(Outbox)</b> 가 필요하다.
     */
    @Bean(MAIL_EXECUTOR)
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("mail-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 종료 시 발송 중인 건은 마무리하되, 배포가 무한정 지연되지 않게 상한을 둔다.
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }
}
