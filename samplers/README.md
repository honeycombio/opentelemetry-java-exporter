# OpenTelemetry Deterministic Sampler

An OpenTelemetry trace sampler that uses a determinisitc algortihm based on a trace ID and provides a consistent result with the Honeycomb Beelines.

## Installation

Maven
```
<dependency>
    <groupId>io.honeycomb.opentelemetry</groupId>
    <artifactId>opentelemetry-samplers-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

Gradle
```
dependencies {
    compile group: 'io.honeycomb.opentelemetry', name: 'opentelemetry-samplers-java', version: '0.1.0'
}
```

## Configuration

```java
// create the sampler, using a rate of 1 in 10 events to be sampled
Sampler sampler = new DeterministicTraceSampler(10);

// create a trace config and set the sampler
TraceConfig traceConfig = TraceConfig.getDefault().toBuilder().setSampler(
    sampler
).build();

// update the OpenTelemetrySdk's trace config
OpenTelemetrySdk.getTracerProvider().updateActiveTraceConfig(
    traceConfig
);
```

## Example

An example is available [here](./src/test/java/io/honeycomb/opentelemetry/examples/DeterministicSamplerExample.java).
