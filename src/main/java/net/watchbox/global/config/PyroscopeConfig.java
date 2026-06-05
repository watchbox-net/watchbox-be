package net.watchbox.global.config;

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.pyroscope.http.Format;
import io.pyroscope.javaagent.EventType;
import io.pyroscope.javaagent.PyroscopeAgent;
import io.pyroscope.javaagent.config.Config;
import io.otel.pyroscope.PyroscopeOtelConfiguration;
import io.otel.pyroscope.PyroscopeOtelSpanProcessor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Pyroscope continuous profiling 설정.
 *
 * <ul>
 *   <li>{@link #startPyroscope()} — 프로파일링 agent 시작 (CPU 프로파일 주기 전송)</li>
 *   <li>{@link #pyroscopeOtelSpanProcessor()} — OTel trace ↔ profile 연동, <b>루트 span 만</b> 관측</li>
 * </ul>
 *
 * {@code pyroscope.enabled=true} 일 때만 활성화.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "pyroscope", name = "enabled", havingValue = "true")
public class PyroscopeConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${pyroscope.server.address}")
    private String serverAddress;

    /** Pyroscope agent 시작 */
    @PostConstruct
    public void startPyroscope() {
        PyroscopeAgent.start(
                new Config.Builder()
                        .setApplicationName(applicationName)
                        .setProfilingEvent(EventType.ITIMER)
                        .setFormat(Format.JFR)
                        .setServerAddress(serverAddress)
                        .build()
        );
        log.info("Pyroscope agent started: app={}, server={}", applicationName, serverAddress);
    }

    /**
     * OTel ↔ Pyroscope 연동 SpanProcessor 를 SDK TracerProvider 에 등록.
     *
     * <p>{@code setRootSpanOnly(true)}: <b>루트 로컬 span 에만</b> {@code pyroscope.profile.id}
     * 를 붙인다. 자식 span 마다 연결하지 않아 오버헤드·카디널리티가 줄고, trace → profile 은
     * 요청 단위(루트)로만 이어진다.
     *
     * <p><b>중요</b>: OpenTelemetry Spring Boot starter 는 SDK autoconfigure 를 사용하므로
     * 단순 {@code @Bean SpanProcessor} 는 TracerProvider 에 자동 등록되지 <b>않는다</b>.
     * {@link AutoConfigurationCustomizerProvider} 로 {@code addTracerProviderCustomizer} 를
     * 통해 명시적으로 {@code addSpanProcessor} 해야 한다.
     *
     * <p>{@code io.pyroscope:otel} 모듈 전체가 deprecated 이지만(Grafana 가 span profiles 를
     * collector 측 경로로 이전 중), 이 의존성으로 span↔profile 연동을 하는 유일한 수단이라
     * 대체 API 가 없다. 동작에는 문제없어 경고만 억제한다.
     */
    @Bean
    @SuppressWarnings("deprecation")
    public AutoConfigurationCustomizerProvider pyroscopeTracerCustomizer() {
        return customizer -> customizer.addTracerProviderCustomizer((builder, configProperties) -> {
            PyroscopeOtelConfiguration config = new PyroscopeOtelConfiguration.Builder()
                    .setRootSpanOnly(true)    // ← 루트 스팬만 관측
                    .setAddSpanName(true)
                    .build();
            return builder.addSpanProcessor(new PyroscopeOtelSpanProcessor(config));
        });
    }
}
