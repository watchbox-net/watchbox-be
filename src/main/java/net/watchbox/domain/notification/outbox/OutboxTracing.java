package net.watchbox.domain.notification.outbox;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * outbox 를 사이에 둔 <b>trace 를 잇는다.</b>
 *
 * <p><b>왜 필요한가</b>: 기록(HTTP 요청 스레드)과 발행(릴레이의 가상 스레드)은 시점도 스레드도
 * 다르다. 스레드 로컬 컨텍스트는 그 경계를 못 넘으므로, trace 가 커밋에서 끊기고
 * "이 초대의 알림이 어떻게 됐는지" 를 trace 로 따라갈 수 없다.
 *
 * <p>그래서 <b>기록 시점의 trace 를 행에 함께 적어두고</b>, 발행할 때 그걸 부모로 삼는다.
 * outbox 가 이벤트를 나르듯 trace 도 같이 나르는 셈이다.
 *
 * <p>W3C {@code traceparent} 만 저장한다. tracestate·baggage 는 벤더 확장이라
 * 지금 구성(OTel → Tempo)에서는 없어도 부모-자식이 이어진다.
 */
@Component
@RequiredArgsConstructor
public class OutboxTracing {

    private static final String TRACE_PARENT = "traceparent";

    private final Tracer tracer;
    private final Propagator propagator;

    /**
     * 지금 trace 를 문자열로 굳힌다.
     *
     * @return traceparent, 진행 중인 trace 가 없으면 {@code null}
     *         (스케줄러에서 시작된 흐름 등 — 이어붙일 부모가 없는 게 정상이다)
     */
    public String capture() {
        TraceContext context = tracer.currentTraceContext().context();
        if (context == null) {
            return null;
        }
        Map<String, String> carrier = new HashMap<>();
        propagator.inject(context, carrier, Map::put);
        return carrier.get(TRACE_PARENT);
    }

    /**
     * 저장해둔 trace 를 부모로 삼는 스팬을 연다.
     *
     * <p>부모가 없으면(traceparent 가 null) 새 trace 로 시작한다 — 발행 구간 자체는 보여야 한다.
     */
    public Span startDispatchSpan(String traceParent, String eventId, String eventType) {
        Span.Builder builder = (traceParent == null)
                ? tracer.spanBuilder()
                : propagator.extract(Map.of(TRACE_PARENT, traceParent), Map::get);

        return builder.name("outbox dispatch")
                .tag("outbox.event_id", eventId)
                .tag("outbox.event_type", eventType)
                .start();
    }

    /** 스팬을 현재 컨텍스트로 올린다. 그래야 이후 Kafka produce 가 자식으로 붙는다. */
    public Tracer.SpanInScope withSpan(Span span) {
        return tracer.withSpan(span);
    }
}
