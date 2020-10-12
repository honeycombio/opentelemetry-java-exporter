# OpenTelemetry Deterministic Sampler

An OpenTelemetry trace sampler that uses a determinisitc algortihm based on a trace ID and provides a consistent result with the Honeycomb Beelines.

## Installation

```java
// create the sampler, using a rate of 1 in 10 events to be sampled
Sampler sampler = new DeterministicTraceSampler(10);

// create a trace config and set the sampler
TraceConfig traceConfig = TraceConfig.getDefault().toBuilder().setSampler(
    sampler
).build();

// use the trace config in the OpenTelemetrySdk
OpenTelemetrySdk.getTracerProvider()
    .updateActiveTraceConfig(
        traceConfig
    );
```

## Example

An example is available [here](./src/test/java/io/honeycomb/opentelemetry/examples/DeterministicSamplerExample.java).
