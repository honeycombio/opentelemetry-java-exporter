# The Honeycomb OpenTelemetry Exporter for Java

An OpenTelemery exporter for sending trace data to Honeycomb.

## Installation

Maven
```
<dependency>
    <groupId>io.honeycomb.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

Gradle
```
dependencies {
    compile group: 'io.honeycomb.opentelemetry', name: 'opentelemetry-exporter-java', version: '0.1.0'
}
```

## Configuration

You will need to name your service and provide your Honeycomb API Key. You can optionally set a dataset which is strongly recommended.

```
// Create span exporter
HoneycombSpanExporter honeycombExporter = HoneycombSpanExporter.newBuilder("my-app")
    .writeKey("my-api-key")
    .dataSet("my-dataset")
    .build();

// Set span processor in OpenTelemetrySDK provider using the exporter
OpenTelemetrySdk.getTracerProvider()
    .addSpanProcessor(
        SimpleSpanProcessor.newBuilder(honeycombExporter).build()
    );
```
