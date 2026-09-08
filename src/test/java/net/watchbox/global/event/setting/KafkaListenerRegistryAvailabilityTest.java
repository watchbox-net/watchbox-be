package net.watchbox.global.event.setting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code KafkaListenerEndpointRegistry} 는 {@code @EnableKafka} 가 BeanDefinitionRegistrar 로
 * 등록하는 빈이라 IDE 정적 분석이 못 잡는 경우가 있다. 실제로 주입 가능한지 확인한다.
 */
class KafkaListenerRegistryAvailabilityTest {

    @Test
    @DisplayName("Kafka 자동설정이 KafkaListenerEndpointRegistry 빈을 등록한다")
    void 레지스트리_빈이_존재한다() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(KafkaAutoConfiguration.class))
                .withPropertyValues("spring.kafka.bootstrap-servers=localhost:9092")
                .run(context -> assertThat(context).hasSingleBean(KafkaListenerEndpointRegistry.class));
    }
}
