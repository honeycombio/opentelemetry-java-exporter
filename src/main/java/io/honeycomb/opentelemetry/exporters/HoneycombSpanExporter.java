package io.honeycomb.opentelemetry.exporters;

import io.honeycomb.libhoney.Event;
import io.honeycomb.libhoney.HoneyClient;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class HoneycombSpanExporter implements SpanExporter {

    final HoneyClient client;

    public HoneycombSpanExporter(final HoneyClient client) {
        if (client == null) {
            throw new IllegalArgumentException();
        }
        this.client = client;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> openTelemetrySpans) {
        for (SpanData span : openTelemetrySpans) {
            createHoneycombEvent(client, span).sendPresampled();
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

    static Event createHoneycombEvent(HoneyClient client, SpanData span) {
        long start = TimeUnit.NANOSECONDS.toMillis(span.getStartEpochNanos());
        long duration = TimeUnit.NANOSECONDS.toMillis(Math.max(1, span.getEndEpochNanos() - span.getStartEpochNanos()));

        final Event event = client.createEvent()
            .setTimestamp(start)
            .addField(AttributeNames.TRACE_ID_FIELD, span.getTraceId().toLowerBase16())
            .addField(AttributeNames.SPAN_ID_FIELD, span.getSpanId().toLowerBase16())
            .addField(AttributeNames.DURATION_FIELD, duration);

        if (!span.getName().isEmpty()) {
            event.addField(AttributeNames.SPAN_NAME_FIELD, span.getName());
        }
        if (span.getParentSpanId().isValid()) {
            event.addField(AttributeNames.PARENT_ID_FIELD, span.getParentSpanId().toLowerBase16());
        }
        if (span.getKind() != null) {
            event.addField(AttributeNames.TYPE_FIELD, span.getKind().name());
        }

        // span attributes
        span.getAttributes().forEach(
            (key, value) -> { addAttributeAsFields(event, key, value); }
        );

        // resource attributes
        if (span.getResource() != null) {
            span.getResource().getAttributes().forEach(
                (key, value) -> { addAttributeAsFields(event, key, value); }
            );
        }

        return event;
    }

    static void addAttributeAsFields(Event event, String key, AttributeValue value) {
        switch(value.getType()) {
            case STRING:
                event.addField(key, value.getStringValue());
                break;
            case LONG:
                event.addField(key, value.getLongValue());
                break;
            case BOOLEAN:
                event.addField(key, value.getBooleanValue());
                break;
            case DOUBLE:
                event.addField(key, value.getDoubleValue());
                break;
        }
    }
}
