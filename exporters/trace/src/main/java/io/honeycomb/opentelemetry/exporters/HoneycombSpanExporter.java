package io.honeycomb.opentelemetry.exporters;

import io.honeycomb.libhoney.Event;
import io.honeycomb.libhoney.HoneyClient;
import io.opentelemetry.common.AttributeValue;
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

    private static void addAttributeAsFields(final Event event, final String key, final AttributeValue value) {
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

    public static HoneycombSpanExporterBuilder newBuilder(String serviceName) {
        return new HoneycombSpanExporterBuilder(serviceName);
    }
}
