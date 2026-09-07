package net.watchbox.global.config;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(basePackages = "net.watchbox")
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    /**
     * Lettuce ClientResources - Micrometer Tracing 주입.
     * 모든 Redis 명령(GET/SET/DEL 등)이 OTel span 으로 자동 생성됨.
     */
    @Bean(destroyMethod = "shutdown")
    public ClientResources lettuceClientResources(ObservationRegistry observationRegistry) {
        return DefaultClientResources.builder()
                .tracing(new MicrometerTracing(observationRegistry, "Redis"))
                .build();
    }

    @Bean // Lettuce - 토큰 저장, 캐싱에 사용
    public LettuceConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isBlank()) {
            serverConfig.setPassword(password);
        }

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientResources(clientResources)
                .build();

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * TMDB 응답 캐시용 논블로킹 템플릿.
     *
     * <p>TMDB 호출은 WebClient 리액티브 체인 안에서 일어나므로, 그 안에서 캐시를 조회하려면
     * 블로킹 {@link StringRedisTemplate} 이 아니라 리액티브 템플릿을 써야 한다.
     * (블로킹 호출을 쓰면 Netty 이벤트 루프를 점유한다)
     */
    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(LettuceConnectionFactory connectionFactory) {
        return new ReactiveStringRedisTemplate(connectionFactory);
    }
}
