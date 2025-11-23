# Instrumentation Library

A lightweight instrumentation library for Java applications that provides runtime metric collection through annotations.

## Features

- **@Instrumented Annotation**: Mark methods for automatic metric collection
- **AspectJ Integration**: Automatic interception of annotated methods
- **Pluggable Metrics**: Custom metric emitter implementations
- **Spring Integration**: Ready-to-use Spring configuration

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.ravi9a2</groupId>
    <artifactId>instrumentation</artifactId>
    <version>0.0.1-RELEASE</version>
</dependency>
```

### 2. Enable AspectJ Auto-Proxy

The library includes `InstrumentationConfig` which automatically enables AspectJ. If you're using Spring Boot, it will be auto-configured.

### 3. Use @Instrumented Annotation

```java
@Service
public class UserService {
    
    @Instrumented(
        metricType = MetricType.HTTP,
        tagSet = "path=/api/users,method=getUser,service=user-service"
    )
    public User getUser(String id) {
        // Your implementation
    }
}
```

## Generic MetricEmitter

The library provides a generic `MetricEmitter` interface that supports multiple metric types:

- **Counters**: Increment counters (e.g., request counts, error counts)
- **Latency**: Record execution time/duration
- **Gauges**: Set, increment, or decrement gauge values (e.g., cache size, queue depth)
- **Histograms**: Record distribution of values

### Custom Metric Emitter

By default, the library uses a no-op metric emitter. To integrate with your metrics system, implement the `MetricEmitter` interface:

```java
@Component
public class CustomMetricEmitter implements MetricEmitter {
    
    private final String metricsEndpoint;
    
    public CustomMetricEmitter(@Value("${instrumentation.metrics.endpoint}") String endpoint) {
        this.metricsEndpoint = endpoint;
        // Initialize connection to your metrics backend
    }
    
    @Override
    public void incrementCounter(String metricName, Map<String, String> tags) {
        incrementCounter(metricName, 1.0, tags);
    }
    
    @Override
    public void incrementCounter(String metricName, double value, Map<String, String> tags) {
        // Emit counter metric to your metrics system
        // e.g., Prometheus, StatsD, CloudWatch, etc.
    }
    
    @Override
    public void recordLatency(String metricName, long latency, Map<String, String> tags) {
        // Emit latency metric (histogram/timer)
    }
    
    @Override
    public void setGauge(String metricName, double value, Map<String, String> tags) {
        // Set gauge value
    }
    
    @Override
    public void incrementGauge(String metricName, double value, Map<String, String> tags) {
        // Increment gauge
    }
    
    @Override
    public void decrementGauge(String metricName, double value, Map<String, String> tags) {
        // Decrement gauge
    }
    
    @Override
    public void recordHistogram(String metricName, double value, Map<String, String> tags) {
        // Record histogram value
    }
}
```

### Configuration

Configure the metrics endpoint in your `application.properties`:

```properties
# Enable/disable metrics (default: true)
instrumentation.metrics.enabled=true

# Metrics server endpoint (for custom implementations)
instrumentation.metrics.endpoint=http://localhost:9091/metrics
```

The host service only needs to:
1. Create a `MetricEmitter` bean implementing the interface
2. Configure the metrics endpoint/server
3. The library handles the rest automatically

### Example: Prometheus Integration

See `PrometheusMetricEmitter` in the examples package for a reference implementation.

## Metrics Collected

The aspect automatically collects the following metrics:

- **instrumented.latency**: Method execution time (histogram/timer)
  - Automatically tracks: count, min, max, percentiles, mean
  - The count from this metric gives you total invocations
- **instrumented.success**: Count of successful invocations (counter)
- **instrumented.failure**: Count of failed invocations (counter)

**Note**: The latency histogram automatically tracks the total invocation count, so there's no separate "total" metric needed. You can get the total by summing success + failure, or by using the count from the latency histogram.

All metrics include tags from the `tagSet` parameter plus:
- `method`: Method name
- `class`: Class simple name
- `metricType`: Metric type (HTTP, RPC, etc.)
- `exception`: Exception class name (on failure)

## Example

```java
@ExternalService
public interface PaymentService {
    @PostCall(path = "/api/payments", service = "payment-service")
    Payment processPayment(@Payload PaymentRequest request);
}
```

The generated implementation will automatically include:

```java
@Instrumented(
    metricType = MetricType.HTTP,
    tagSet = "path=/api/payments,httpMethod=POST,method=processPayment,service=payment-service"
)
@Override
public Payment processPayment(PaymentRequest request) {
    // Generated implementation
}
```

The aspect will automatically collect metrics for this method.

## Configuration

The library is auto-configured when Spring is present. To disable:

```java
@SpringBootApplication
@EnableAutoConfiguration(exclude = InstrumentationConfig.class)
public class Application {
    // ...
}
```

## License

[Your License Here]

