package net.watchbox.global.event.setting;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.watchbox.global.event.EventTransport;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 전송 경로 설정의 단일 소유자. 조회·전환·Kafka 컨슈머 제어를 한 곳에서 한다.
 *
 * <p><b>조회는 캐시에서만</b> 한다({@link #current()}). 이벤트마다 DB 를 치면 발행 경로가
 * 곧 병목이자 장애점이 되기 때문이다. DB 는 재기동 후에도 설정이 남게 하는 저장소일 뿐이다.
 *
 * <p><b>Kafka 컨슈머는 KAFKA 모드일 때만 살려둔다.</b> 브로커가 꺼진 상태에서 컨슈머가
 * 살아있으면 무한 재연결로 로그가 폭발한다. 그래서 리스너는 {@code autoStartup = "false"} 로
 * 두고 여기서 start/stop 한다.
 *
 * <p><b>기동 시</b> DB 값이 KAFKA 여도 브로커가 닿지 않으면 LOCAL 로 시작한다.
 * 브로커는 비용 때문에 평소 꺼두므로, 앱이 못 뜨거나 느리게 뜨는 일이 없어야 한다.
 * 이때 DB 값은 바꾸지 않는다 — 나중에 브로커를 켜고 전환 API 로 재시도할 수 있어야 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventTransportSettings {

    /** {@code EventTransportHealthCheckListener} 의 리스너 id 와 같아야 한다. */
    public static final String DOMAIN_EVENT_LISTENER_ID = "domain-events";

    private static final int BROKER_PROBE_TIMEOUT_MS = 3_000;

    private final EventTransportSettingRepository repository;
    private final KafkaListenerEndpointRegistry listenerRegistry;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /** 발행 경로에서 호출된다. 절대 DB 를 치지 않는다. */
    private final AtomicReference<EventTransport> cached = new AtomicReference<>(EventTransport.LOCAL);

    public EventTransport current() {
        return cached.get();
    }

    /** DB 에 저장된 설정. 캐시된 실제 동작값({@link #current()})과 다를 수 있다(기동 시 폴백된 경우). */
    @Transactional(readOnly = true)
    public EventTransport configured() {
        return repository.findById(EventTransportSetting.SINGLETON_ID)
                .map(EventTransportSetting::getTransport)
                .orElse(EventTransport.LOCAL);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void applyOnStartup() {
        EventTransport configured = loadOrInitialize();
        if (configured == EventTransport.KAFKA && !isBrokerReachable()) {
            log.warn("event transport configured=KAFKA but broker unreachable, starting with LOCAL - servers={}",
                    bootstrapServers);
            apply(EventTransport.LOCAL);
            return;
        }
        apply(configured);
        log.info("event transport initialized - transport={}", configured);
    }

    /**
     * 저장된 설정을 읽되, 없으면 기본값(LOCAL) 행을 만들어 둔다.
     *
     * <p>"행이 없으면 LOCAL" 로 두면 DB 만 봤을 때 <b>설정이 누락된 건지 기본값인지 구분이 안 된다.</b>
     * 현재 동작을 DB 에서 바로 확인할 수 있어야 운영·디버깅이 쉽다.
     */
    private EventTransport loadOrInitialize() {
        return repository.findById(EventTransportSetting.SINGLETON_ID)
                .map(EventTransportSetting::getTransport)
                .orElseGet(() -> {
                    repository.save(EventTransportSetting.of(EventTransport.LOCAL));
                    log.info("event transport setting row initialized - transport=LOCAL");
                    return EventTransport.LOCAL;
                });
    }

    /**
     * 전송 경로를 바꾼다. DB 저장 → 컨슈머 제어 → 캐시 갱신 순서다.
     *
     * <p>KAFKA 로 바꿀 때 브로커가 닿지 않으면 전환하지 않고 예외를 던진다.
     * 조용히 로컬로 남으면 "바꿨는데 왜 안 되지" 를 디버깅하게 된다.
     */
    @Transactional
    public EventTransport switchTo(EventTransport target) {
        if (target == EventTransport.KAFKA && !isBrokerReachable()) {
            throw new IllegalStateException("Kafka broker unreachable: " + bootstrapServers);
        }

        EventTransportSetting setting = repository.findById(EventTransportSetting.SINGLETON_ID)
                .orElseGet(() -> EventTransportSetting.of(target));
        setting.changeTo(target);
        repository.save(setting);

        apply(target);
        log.info("event transport switched - transport={}", target);
        return target;
    }

    /** 컨슈머 상태를 목표 경로에 맞추고 캐시를 갱신한다. */
    private void apply(EventTransport target) {
        MessageListenerContainer container = listenerRegistry.getListenerContainer(DOMAIN_EVENT_LISTENER_ID);
        if (container == null) {
            log.warn("domain event listener container not found - id={}", DOMAIN_EVENT_LISTENER_ID);
        } else if (target == EventTransport.KAFKA) {
            if (!container.isRunning()) container.start();
        } else {
            if (container.isRunning()) container.stop();
        }
        cached.set(target);
    }

    /** 브로커 접속 가능 여부. 기동·전환 시 한 번씩만 호출한다(발행 경로에서는 호출 금지). */
    public boolean isBrokerReachable() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, BROKER_PROBE_TIMEOUT_MS);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, BROKER_PROBE_TIMEOUT_MS);

        try (AdminClient admin = AdminClient.create(props)) {
            admin.describeCluster(new DescribeClusterOptions().timeoutMs(BROKER_PROBE_TIMEOUT_MS))
                    .nodes()
                    .get(BROKER_PROBE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception e) {
            // debug 로 두면 정작 진단이 필요한 순간(기동 폴백·전환 거절)에 원인이 안 보인다.
            // 기동·전환 시에만 호출되므로 로그가 쌓이지도 않는다.
            log.warn("kafka broker probe failed - servers={}, cause={}", bootstrapServers, e.toString());
            return false;
        }
    }
}
