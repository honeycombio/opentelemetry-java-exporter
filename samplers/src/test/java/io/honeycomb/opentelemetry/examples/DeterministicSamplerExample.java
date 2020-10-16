package io.honeycomb.opentelemetry.examples;

import io.honeycomb.opentelemetry.samplers.DeterministicTraceSampler;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Tracer;
import java.util.Random;

public class DeterministicSamplerExample {

    public static void main(String[] args) throws InterruptedException {

        // 1. Create an instance of the Deterministic sampler
        Sampler sampler = new DeterministicTraceSampler(10);

        // 2. Create a copy of the OpenTelemtrySdk trace config and set the sampler
        TraceConfig traceConfig = TraceConfig.getDefault().toBuilder().setSampler(sampler).build();

        // 3. Update the OpenTelemetrySdk with the trace config
        OpenTelemetrySdk.getTracerManagement().updateActiveTraceConfig(
            traceConfig
        );

        // 4. Create an OpenTelemetry `Tracer` that can be used to create spans
        Tracer tracer = OpenTelemetry.getTracerProvider().get("sample-app", "1.0");

        // 5. Manually instrument some work
        doWork(tracer);
    }

    private static void doWork(Tracer tracer) throws InterruptedException {
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            Span parentSpan = tracer.spanBuilder("parentSpan").setSpanKind(Kind.CLIENT).startSpan();
            try (Scope parentScope = tracer.withSpan(parentSpan)) {
                // create child span
                Span childSpan = tracer.spanBuilder("childSpan").setSpanKind(Kind.SERVER).startSpan();
                try (Scope childScope = tracer.withSpan(childSpan)) {
                    childSpan.setAttribute("attr", "my-attr");

                    Thread.sleep(random.nextInt(1000));
                    childSpan.end();
                }

                Thread.sleep(random.nextInt(1000));
                parentSpan.end();
            }
        }
    }
}
