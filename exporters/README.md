# Honeycomb OpenTelemetry Trace Exporter for Java

An OpenTelemery span exporter for sending trace data to Honeycomb.

## Installation

Maven
```
<dependency>
    <groupId>io.honeycomb.opentelemetry</groupId>
    <artifactId>opentelemetry-exporters-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

Gradle
```
dependencies {
    compile group: 'io.honeycomb.opentelemetry', name: 'opentelemetry-exporters-java', version: '0.1.0'
}
```

## Configuration

You will need to name your service and provide your Honeycomb API Key. You can optionally set a dataset which is strongly recommended.

```java
// Create span exporter
HoneycombSpanExporter exporter = HoneycombSpanExporter.newBuilder("my-app")
    .writeKey("my-api-key")
    .dataSet("my-dataset")
    .build();

// Set span processor in OpenTelemetrySdk provider using the exporter
OpenTelemetrySdk.getTracerManagement().addSpanProcessor(
    SimpleSpanProcessor.newBuilder(exporter).build()
);
```

## Example

An example is available [here](./src/test/java/io/honeycomb/opentelemetry/examples/SpanExporterExample.java).
