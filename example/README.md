# Network Framework Example

This example project demonstrates how to use the network framework libraries:

1. **Code Generation**: Using `@ExternalService` and HTTP/gRPC annotations
2. **Reactive Services**: Using `@GetCall`/`@PostCall` with WebClient (Mono/Flux)
3. **Non-Reactive Services**: Using `@GetCall`/`@PostCall` with HttpClient (blocking)
4. **gRPC Services**: Using `@UnaryRPCCall` for gRPC unary calls
5. **Hystrix Wrapper**: Using Hystrix executors for circuit breaker and thread pool isolation
6. **Metrics Collection**: Using `@Instrumented` annotation for automatic metrics
7. **Resilience Patterns**: Circuit breaker and bulkhead configuration (R4J and Hystrix)
8. **Client Configuration**: Multiple client types setup and configuration

## Project Structure

```
example/
├── src/main/java/com/ravi9a2/example/
│   ├── ExampleApplication.java          # Spring Boot main class
│   ├── controller/
│   │   └── ExampleController.java       # REST controller
│   ├── service/
│   │   ├── UserService.java             # Reactive service (WebClient) - code generated
│   │   ├── ProductService.java          # Non-reactive service (HttpClient) - code generated
│   │   ├── OrderService.java            # gRPC service - code generated
│   │   ├── PaymentService.java          # Payment service (can use Hystrix) - code generated
│   │   ├── ExampleService.java          # Service using @Instrumented
│   │   └── HystrixExampleService.java   # Direct Hystrix executor usage examples
│   ├── model/
│   │   └── User.java                    # Data model
│   └── config/
│       ├── ExampleMetricEmitter.java    # Custom MetricEmitter implementation
│       ├── ResilienceConfig.java       # Circuit breaker & bulkhead config (R4J)
│       └── ClientConfig.java            # Client registry configuration
├── src/main/resources/
│   └── application.properties            # Configuration (R4J and Hystrix)
├── README.md                             # Main documentation
└── HYSTRIX_EXAMPLE.md                    # Hystrix-specific examples
```

## Features Demonstrated

### 1. Reactive Service (WebClient)

The `UserService` interface demonstrates reactive HTTP calls using WebClient:

```java
@ExternalService
public interface UserService {
    @GetCall(
        path = "/api/users/{userId}",
        service = "user-service",
        cbEnabled = true,
        circuitBreaker = "user-service-cb"
    )
    Mono<User> getUser(@PathParam("userId") String userId);
}
```

**Key Points:**
- Returns `Mono<T>` or `Flux<T>` for reactive streams
- Uses `ReactiveClientRegistry` and `ReactiveExecutor`
- Non-blocking, asynchronous calls
- Generated code uses WebClient wrapper

### 2. Non-Reactive Service (HttpClient)

The `ProductService` interface demonstrates blocking HTTP calls using Apache HttpClient:

```java
@ExternalService
public interface ProductService {
    @GetCall(
        path = "/api/products/{productId}",
        service = "product-service",
        cbEnabled = true,
        circuitBreaker = "product-service-cb"
    )
    User getProduct(@PathParam("productId") String productId);
}
```

**Key Points:**
- Returns regular types (not Mono/Flux) - blocking calls
- Uses `NonReactiveClientRegistry` and `NonReactiveExecutor`
- Synchronous, blocking calls
- Generated code uses HttpClient wrapper
- Suitable for traditional blocking code

### 3. gRPC Service

The `OrderService` interface demonstrates gRPC unary calls:

```java
@ExternalService
public interface OrderService {
    @UnaryRPCCall(
        service = "order-service",
        cbEnabled = true,
        circuitBreaker = "order-service-cb",
        fqPackageName = "com.ravi9a2.example.proto",
        className = "OrderService",
        methodName = "GetOrder",
        rpcMethod = RPCMethod.UNARY
    )
    User getOrder(@PathParam("orderId") String orderId);
}
```

**Key Points:**
- Uses `@UnaryRPCCall` or `@BidiRPCCall` annotations
- Uses `RPCClientRegistry` and `RPCExecutor`
- Can return blocking types or `ListenableFuture` for async
- Requires gRPC client wrapper implementation (GrpcClientWrapper)
- Generated code handles gRPC-specific details

### 4. Code Generation

The annotation processor automatically generates implementations for all three service types with:
- Appropriate HTTP/gRPC client calls
- Circuit breaker protection
- Bulkhead isolation
- Automatic `@Instrumented` annotation for metrics

### 5. Metrics Collection

The `ExampleService` demonstrates using `@Instrumented` annotation:

```java
@Instrumented(
    metricType = MetricType.HTTP,
    tagSet = "operation=processUser,service=example-service"
)
public Mono<User> processUser(String userId) {
    // Method implementation
}
```

This automatically collects:
- Latency metrics (histogram with count, percentiles, etc.)
- Success/failure tracking via status tags
- All tags from the annotation

### 6. Custom Metric Emitter

The `ExampleMetricEmitter` shows how to implement a custom metrics backend:

```java
@Component
public class ExampleMetricEmitter implements MetricEmitter {
    // Implement all MetricEmitter methods
    // In production, send to Prometheus, CloudWatch, etc.
}
```

### 7. Configuration

All configuration is in `application.properties`:
- Downstream service URLs and timeouts
- Circuit breaker settings
- Bulkhead settings
- Metrics endpoint configuration

## Running the Example

1. **Build the project**:
   ```bash
   mvn clean install
   ```

2. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

3. **Test the endpoints**:
   ```bash
   # Reactive service (WebClient)
   curl http://localhost:8080/api/example/users/1
   
   # Non-reactive service (HttpClient - blocking)
   curl http://localhost:8080/api/example/products/1
   
   # gRPC service (requires gRPC server)
   curl http://localhost:8080/api/example/orders/1
   
   # Payment service (with Hystrix protection)
   curl http://localhost:8080/api/example/payments/1
   
   # Hystrix direct usage (reactive)
   curl http://localhost:8080/api/example/hystrix/payments/1
   
   # Hystrix direct usage (blocking)
   curl http://localhost:8080/api/example/hystrix/payments-blocking/1
   
   # Test Hystrix circuit breaker
   curl http://localhost:8080/api/example/hystrix/test-circuit-breaker?fail=true
   
   # Database operation
   curl http://localhost:8080/api/example/users
   
   # Risky operation (may fail)
   curl http://localhost:8080/api/example/risky?fail=false
   curl http://localhost:8080/api/example/risky?fail=true
   ```

4. **View metrics**:
   - Check the console logs for metric emissions
   - The `ExampleMetricEmitter` logs all metrics with emojis for easy identification

## What You'll See

### Generated Code

After compilation, you'll find generated implementations in `target/generated-sources/annotations/`:

- **UserServiceImpl**: Reactive service using `ReactiveClientRegistry` and `ReactiveExecutor`
- **ProductServiceImpl**: Non-reactive service using `NonReactiveClientRegistry` and `NonReactiveExecutor`
- **OrderServiceImpl**: gRPC service using `RPCClientRegistry` and `RPCExecutor`

All generated implementations:
- Use appropriate client registries
- Apply circuit breaker and bulkhead via executors
- Have `@Instrumented` annotations automatically added

### Metrics Output

You'll see console output like:
```
📊 COUNTER: instrumented.latency = 1 (tags: {operation=processUser, service=example-service})
⏱️  LATENCY: instrumented.latency = 150ms (tags: {method=getUser, class=UserServiceImpl, status=true})
```

### Resilience Patterns

- Circuit breaker will open if failures exceed threshold
- Bulkhead will limit concurrent calls
- All failures are tracked in metrics

## Next Steps

1. **Implement Real Metric Emitter**: Replace `ExampleMetricEmitter` with Prometheus, CloudWatch, or StatsD implementation
2. **Add More Services**: Create more `@ExternalService` interfaces
3. **Configure Monitoring**: Set up dashboards using the emitted metrics
4. **Add Retry**: Implement retry mechanism in executors

## Service Types Comparison

| Feature | Reactive (WebClient) | Non-Reactive (HttpClient) | gRPC |
|---------|---------------------|--------------------------|------|
| Return Type | `Mono<T>` / `Flux<T>` | `T` (blocking) | `T` or `ListenableFuture<T>` |
| Execution | Non-blocking, async | Blocking, sync | Blocking or async |
| Client Registry | `ReactiveClientRegistry` | `NonReactiveClientRegistry` | `RPCClientRegistry` |
| Executor | `ReactiveExecutor` | `NonReactiveExecutor` | `RPCExecutor` |
| Use Case | Reactive/Spring WebFlux apps | Traditional blocking apps | Microservice communication |
| Annotation | `@GetCall` / `@PostCall` | `@GetCall` / `@PostCall` | `@UnaryRPCCall` / `@BidiRPCCall` |

## Hystrix Examples

The project includes comprehensive Hystrix examples:

- **PaymentService**: Code-generated service that can use Hystrix executors
- **HystrixExampleService**: Direct usage of Hystrix executors (reactive and blocking)
- **Configuration**: Hystrix circuit breaker and thread pool configuration in `application.properties`

See [HYSTRIX_EXAMPLE.md](HYSTRIX_EXAMPLE.md) for detailed Hystrix usage examples.

## Notes

- The example uses `jsonplaceholder.typicode.com` as a mock external service
- All metrics are logged to console for demonstration
- In production, implement a real `MetricEmitter` that sends to your metrics backend
- **gRPC Note**: The `OrderService` example requires a gRPC client wrapper implementation (`GrpcClientWrapper`). The `ClientConfig` provides a placeholder `RPCClientRegistry` that will need to be replaced with actual gRPC client implementations.
- **Hystrix Note**: Hystrix is in maintenance mode. The Hystrix wrapper is provided as an alternative for existing systems. For new projects, consider using Resilience4j (R4J wrapper).

