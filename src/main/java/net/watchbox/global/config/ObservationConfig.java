package net.watchbox.global.config;

import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
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
    ObservationPredicate noiseObservationFilter() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext ctx) {
                String uri = ctx.getCarrier().getRequestURI();
                return !"/health".equals(uri);
            }
            return true;
        };
    }
}
