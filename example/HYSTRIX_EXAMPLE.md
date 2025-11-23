# Hystrix Wrapper Example

This document demonstrates how to use the Hystrix wrapper in the example project.

## Overview

The example project includes several ways to use Hystrix:

1. **Code-Generated Service** (`PaymentService`) - Uses `@ExternalService` annotation, will use Hystrix if Hystrix executors are configured
2. **Direct Hystrix Usage** (`HystrixExampleService`) - Shows how to use Hystrix executors directly

## Configuration

Hystrix configuration is loaded from `application.properties`:

```properties
# Default circuit breaker
hystrix.circuit-breaker.default.circuitBreakerEnabled=true
hystrix.circuit-breaker.default.circuitBreakerRequestVolumeThreshold=20
hystrix.circuit-breaker.default.circuitBreakerSleepWindowInMilliseconds=5000
hystrix.circuit-breaker.default.circuitBreakerErrorThresholdPercentage=50

# Service-specific circuit breaker
hystrix.circuit-breaker.payment-service-cb.circuitBreakerRequestVolumeThreshold=10
hystrix.circuit-breaker.payment-service-cb.circuitBreakerErrorThresholdPercentage=30

# Default thread pool
hystrix.thread-pool.default.coreSize=10
hystrix.thread-pool.default.maximumSize=10

# Service-specific thread pool
hystrix.thread-pool.payment-service-bh.coreSize=15
hystrix.thread-pool.payment-service-bh.maximumSize=20
```

## Endpoints

### 1. Payment Service (Code-Generated)

```bash
# Get payment using code-generated service
curl http://localhost:8080/api/example/payments/1
```

This uses `PaymentService` interface which is code-generated. The generated implementation will use Hystrix executors if they are available in the Spring context.

### 2. Direct Hystrix Reactive Executor

```bash
# Get payment using Hystrix reactive executor directly
curl http://localhost:8080/api/example/hystrix/payments/1
```

This demonstrates direct usage of `HystrixReactiveExecutor` for reactive calls.

### 3. Direct Hystrix Non-Reactive Executor

```bash
# Get payment using Hystrix non-reactive executor (blocking)
curl http://localhost:8080/api/example/hystrix/payments-blocking/1
```

This demonstrates direct usage of `HystrixNonReactiveExecutor` for blocking calls.

### 4. Test Circuit Breaker

```bash
# Test circuit breaker with success
curl http://localhost:8080/api/example/hystrix/test-circuit-breaker?fail=false

# Test circuit breaker with failure (will trigger circuit breaker)
curl http://localhost:8080/api/example/hystrix/test-circuit-breaker?fail=true
```

This demonstrates Hystrix circuit breaker behavior. After multiple failures, the circuit will open and return fallback responses.

## Code Examples

### Direct Hystrix Usage

```java
@Autowired
private HystrixReactiveExecutor<WebClient> hystrixReactiveExecutor;

@Autowired
private ReactiveClientRegistry reactiveClientRegistry;

public Mono<User> getPaymentWithHystrix(String paymentId) {
    // Get the client
    ReactiveClient<WebClient> client = reactiveClientRegistry.client("payment-service");
    
    // Build call definition
    RestCallDefinition callDefinition = RestCallDefinition.builder()
            .isCircuitBreakerEnabled(true)
            .isBulkheadEnabled(true)
            .serviceTag("payment-service")
            .cbTag("payment-service-cb")
            .bhTag("payment-service-bh")
            .path("/api/payments/{paymentId}")
            .responseType(User.class)
            .httpMethod(HTTPMethod.GET)
            .build();
    
    // Execute with Hystrix
    return hystrixReactiveExecutor.executeToMono(client, callDefinition);
}
```

## Hystrix vs R4J

Both Hystrix and R4J wrappers can coexist. The framework will use whichever executor is available:

- If both are configured, the code-generated services will use the first executor found
- For direct usage, you can explicitly inject the executor you want to use
- Configuration is separate: `hystrix.*` for Hystrix, `resilience4j.*` for R4J

## Monitoring

Hystrix provides a metrics event stream that can be consumed for monitoring:

```bash
# Hystrix metrics stream (if configured)
curl http://localhost:8080/hystrix.stream
```

## Notes

- Hystrix is in maintenance mode. For new projects, consider using Resilience4j (R4J wrapper)
- The Hystrix wrapper is provided as an alternative for existing Hystrix-based systems
- All Hystrix examples are optional - the application will work without them if Hystrix wrapper is not configured

