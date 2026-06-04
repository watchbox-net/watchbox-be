package net.watchbox.global.config;

import net.watchbox.domain.notification.sse.SseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * SSE 관련 설정.
 *
 * <ul>
 *   <li>{@link SseProperties} 바인딩 활성화 — application.yml 의 {@code notification.sse.*}</li>
 *   <li>추후 SSE 전용 스레드풀 / heartbeat 스케줄러 등 추가 시 여기에 빈 등록</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(SseProperties.class)
public class SseConfig {
    // SSE 전용 스레드풀 (heartbeat / 비동기 send용)
//    @Bean
//    public ScheduledExecutorService sseScheduler(SseProperties props) {
//        return Executors.newScheduledThreadPool(2,
//                r -> new Thread(r, "sse-scheduler"));
//    }

    // WebMvcConfigurer로 async request timeout 설정
    @Bean
    public WebMvcConfigurer sseWebMvcConfigurer(SseProperties props) {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
                configurer.setDefaultTimeout(props.timeout().toMillis());
            }
        };
    }
}
