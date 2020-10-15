package io.honeycomb.opentelemetry.exporters;

import io.honeycomb.libhoney.Event;
import io.honeycomb.libhoney.HoneyClient;
import io.opentelemetry.common.AttributeConsumer;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Strings.isNullOrEmpty;

public class HoneycombSpanExporter implements SpanExporter {

    private final HoneyClient client;
    private final String serviceName;

    public HoneycombSpanExporter(final HoneyClient client, final String serviceName) {
        if (client == null) {
            throw new IllegalArgumentException();
        }
        if (isNullOrEmpty(serviceName)) {
            throw new IllegalArgumentException();
        }
        this.client = client;
        this.serviceName = serviceName;
    }

    @Override
    public CompletableResultCode export(final Collection<SpanData> openTelemetrySpans) {
        for (SpanData span : openTelemetrySpans) {
            createHoneycombEvent(client, serviceName, span).sendPresampled();
        }
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        client.close();
        return CompletableResultCode.ofSuccess();
    }

    static Event createHoneycombEvent(final HoneyClient client, final String serviceName, final SpanData span) {
        long start = TimeUnit.NANOSECONDS.toMillis(span.getStartEpochNanos());
        long duration = TimeUnit.NANOSECONDS.toMillis(Math.max(1, span.getEndEpochNanos() - span.getStartEpochNanos()));

        final Event event = client.createEvent()
            .setTimestamp(start)
            .addField(AttributeNames.SERVICE_NAME_FIELD, serviceName)
            .addField(AttributeNames.TRACE_ID_FIELD, span.getTraceId())
            .addField(AttributeNames.SPAN_ID_FIELD, span.getSpanId())
            .addField(AttributeNames.DURATION_FIELD, duration);

        if (!span.getName().isEmpty()) {
            event.addField(AttributeNames.SPAN_NAME_FIELD, span.getName());
        }
        if (!span.getParentSpanId().isEmpty()) {
            event.addField(AttributeNames.PARENT_ID_FIELD, span.getParentSpanId());
        }
        if (span.getKind() != null) {
            event.addField(AttributeNames.TYPE_FIELD, span.getKind().name());
        }

        // span attributes
        span.getAttributes().forEach(
            new AttributeConsumer() {
                @Override
                public <T> void consume(AttributeKey<T> key, T value) {
                    addAttributeAsField(event, key, value);
                }
            }
        );

        // resource attributes
        if (span.getResource() != null) {
            span.getResource().getAttributes().forEach(
                new AttributeConsumer() {
                    @Override
                    public <T> void consume(AttributeKey<T> key, T value) {
                        addAttributeAsField(event, key, value);
                    }
                }
            );
        }

        return event;
    }

    private static <T> void addAttributeAsField(final Event event, final AttributeKey<T> key, final T value) {
        switch(key.getType()) {
            case STRING:
                event.addField(key.getKey(), (String) value);
                break;
            case LONG:
                event.addField(key.getKey(), (long) value);
                break;
            case BOOLEAN:
                event.addField(key.getKey(), (boolean) value);
                break;
            case DOUBLE:
                event.addField(key.getKey(), (double) value);
                break;
        }
    }

    public static HoneycombSpanExporterBuilder newBuilder(String serviceName) {
        return new HoneycombSpanExporterBuilder(serviceName);
    }
}
