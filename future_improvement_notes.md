1. Header Extraction using Micrometer Propagator
Micrometer provides a Propagator API that extracts trace metadata from any incoming map/headers structure.

When a ConsumerRecord arrives, you pass its headers to tracer.extract(...):

kotlin
// 1. Extract TraceContext from incoming Kafka record headers
val traceContextOrSampling = tracer.extract(record.headers()) { headers, key ->
    headers.lastHeader(key)?.value()?.let { String(it, StandardCharsets.UTF_8) }
}
2. Joining the Trace (Creating Child Span)
You pass the extracted context into tracer.nextSpan(...). Micrometer parses the incoming traceId and creates a child span linked to that exact trace:

kotlin
// 2. Create a span linked to the extracted parent trace ID
val consumerSpan = tracer.nextSpan(traceContextOrSampling)
    .name("kafka-consumer-process")
    .start()
3. Scope Activation & Automatic SLF4J MDC Sync
This is the key step: When you activate the span using tracer.withSpan(consumerSpan).use { scope -> ... }, Micrometer’s internal MDC Listener (SLF4JBridgeHandler / BraveMDCHandler) automatically triggers:

kotlin
// 3. Opening the scope AUTOMATICALLY populates SLF4J MDC!
tracer.withSpan(consumerSpan).use { scope ->
    // Inside this block:
    // Micrometer internally called MDC.put("traceId", consumerSpan.context().traceId())
    
    LOGGER.info("Processing event") // Log automatically prints [traceId=0c4abad3...]!
} // Upon exit: Micrometer automatically calls MDC.remove("traceId")!
Under the Hood (What Micrometer does automatically)
When tracer.withSpan(...) is called, Micrometer executes an internal scope listener that performs the equivalent of:

java
// Micrometer's internal SLF4JBridgeHandler logic (runs automatically):
MDC.put("traceId", currentSpan.context().traceId());
MDC.put("spanId", currentSpan.context().spanId());
And when the .use { } block completes, Micrometer automatically cleans up:

java
MDC.remove("traceId");
MDC.remove("spanId");
Summary
tracer.extract(record.headers()) → Reads X-Correlation-ID or W3C traceparent from Kafka header.
tracer.nextSpan(extractedContext) → Adopts the parent traceId.
tracer.withSpan(span).use { } → Triggers Micrometer's MDC bridge, which automatically populates SLF4J MDC for all log statements inside the block.


Question 2: How to propagate Trace ID across EventHub Kafka WITHOUT modifying every Listener and Producer in Production?
In an existing production project with 50+ listeners and producers, modifying every @KafkaListener and KafkaTemplate call is error-prone.

Instead, you can enable Global Automatic Instrumentation using one of two approaches:

Approach A: Spring Kafka Micrometer Observation (Recommended Spring Boot 3+ Way)
Spring Kafka 3.0+ includes built-in support for Micrometer Observation. When enabled, Spring Kafka automatically intercepts every single outbound and inbound Kafka message across your application:

On Producer (KafkaTemplate): Automatically injects trace headers (traceparent, X-Correlation-ID, b3) into ProducerRecord.headers() before sending.
On Consumer (@KafkaListener): Automatically extracts trace headers from incoming ConsumerRecord, starts a child span, and populates MDC context before calling your @KafkaListener method.
How to enable globally (Zero Java/Kotlin code changes to listeners/producers):

In your application.yml:

yaml
spring:
  kafka:
    template:
      observation-enabled: true  # Auto-injects trace headers on ALL producer calls
    listener:
      observation-enabled: true  # Auto-extracts trace headers & sets MDC on ALL listeners
Prerequisite: Add io.micrometer:micrometer-tracing-bridge-brave (or otel) dependency to your build.gradle / pom.xml.

Approach B: Global Kafka Interceptors (Spring Configuration Level)
If you aren't using Micrometer Observation, you can configure two global Spring Kafka interceptors in your KafkaConfig class:

Global Producer Interceptor (ProducerInterceptor): Attached to the global ProducerFactory. Intercepts every send(...) call across the app and injects X-Correlation-ID header automatically.

Global Consumer Interceptor (RecordInterceptor): Attached to ConcurrentKafkaListenerContainerFactory.setRecordInterceptor(...). Intercepts every message right before it enters any @KafkaListener, sets MDC context, and clears MDC after execution.

kotlin
// Example: Registering RecordInterceptor globally on Listener Factory
@Bean
fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
    val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
    factory.consumerFactory = consumerFactory()
    // Global Record Interceptor for ALL @KafkaListener endpoints
    factory.setRecordInterceptor { record, _ ->
        val traceId = record.headers().lastHeader("X-Correlation-ID")?.value()?.let { String(it) }
        if (traceId != null) {
            MDC.put("traceId", traceId)
        }
        record
    }
    return factory
}
Summary for your Production Project:
For Zero Code Changes: Turn on spring.kafka.template.observation-enabled=true and spring.kafka.listener.observation-enabled=true.
For Custom Centralized Logic: Register a global ProducerInterceptor and RecordInterceptor in your shared Spring KafkaConfig.