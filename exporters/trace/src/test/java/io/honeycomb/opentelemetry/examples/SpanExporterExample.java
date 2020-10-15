package io.honeycomb.opentelemetry.examples;

import io.honeycomb.opentelemetry.exporters.HoneycombSpanExporter;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Tracer;
import java.util.Random;

public class SpanExporterExample {

    public static void main(String[] args) throws InterruptedException {
        String apiKey = System.getenv("HONEYCOMB_APIKEY");
        String dataset = System.getenv("HONEYCOMB_DATASET");

        // 1. Create an instance of the Honeycomb exporter:
        SpanExporter exporter = HoneycombSpanExporter.newBuilder("sample-app")
            .writeKey(apiKey)
            .dataSet(dataset)
            .build();

        // 2. Create an OpenTelemetry span processor using the exporter and set it within the OpenTelemetry SDK
        OpenTelemetrySdk.getTracerManagement().addSpanProcessor(
            SimpleSpanProcessor.newBuilder(exporter).build()
        );

        // 3. Create an OpenTelemetry `Tracer` that can be used to create spans
        Tracer tracer = OpenTelemetry.getTracerProvider().get("sample-app", "1.0");

        // Manually instrument some simple work
        doWork(tracer);

        // Shutdown the exporter before closing. NOTE: This also flushes any pending spans before returning.
        exporter.shutdown();
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
