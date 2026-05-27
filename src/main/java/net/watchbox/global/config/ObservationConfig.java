package net.watchbox.global.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
public class ObservationConfig {
    @Bean
    ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }

    /**
     * /health (docker healthcheck) trace 생성 자체를 차단.
     * Observation 만들기 전에 거르므로 OTLP 로 전송도 안 됨.
     */
    @Bean
    public ObservationRegistryCustomizer<ObservationRegistry> noHealthEndpointObservation() {
        return registry -> registry.observationConfig()
                .observationPredicate((name, context) -> {
                    if (context instanceof ServerRequestObservationContext serverContext) {
                        String uri = serverContext.getCarrier().getRequestURI();
                        // /health 단독 호출은 trace 제외 (/health/infra/* 는 포함)
                        return !"/health".equals(uri);
                    }
                    return true;
                });
    }
}
