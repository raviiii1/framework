# Quick Start Guide

This example project demonstrates the complete network framework in action.

## What This Example Shows

1. **Code Generation**: `UserService` interface with `@ExternalService` - generates implementation automatically
2. **Metrics Collection**: `ExampleService` with `@Instrumented` - automatic metrics collection
3. **Resilience**: Circuit breaker and bulkhead configuration
4. **Custom Metrics**: `ExampleMetricEmitter` - shows how to implement your own metrics backend

## Project Structure

```
example/
├── src/main/java/com/ravi9a2/example/
│   ├── ExampleApplication.java          # Spring Boot entry point
│   ├── controller/
│   │   └── ExampleController.java       # REST endpoints
│   ├── service/
│   │   ├── UserService.java             # External service (code generated)
│   │   └── ExampleService.java          # Service with @Instrumented
│   ├── model/
│   │   └── User.java                    # Data model
│   └── config/
│       ├── ExampleMetricEmitter.java   # Custom metrics implementation
│       └── ResilienceConfig.java       # Circuit breaker config
└── src/main/resources/
    └── application.properties            # All configuration
```

## Key Features Demonstrated

### 1. External Service Interface (Code Generation)

```java
@ExternalService
public interface UserService {
    @GetCall(path = "/api/users/{userId}", service = "user-service")
    Mono<User> getUser(@PathParam("userId") String userId);
}
```

**What happens:**
- Annotation processor generates `UserServiceImpl`
- Implementation includes circuit breaker and bulkhead
- Automatically uses configured WebClient
- All calls are instrumented for metrics

### 2. Instrumented Methods

```java
@Instrumented(metricType = MetricType.HTTP, tagSet = "operation=processUser")
public Mono<User> processUser(String userId) {
    // Your code
}
```

**What happens:**
- `InstrumentedAspect` intercepts the method
- Collects latency (histogram with count, percentiles)
- Adds tags: method, class, metricType, status, exception (if any)

### 3. Custom Metric Emitter

```java
@Component
public class ExampleMetricEmitter implements MetricEmitter {
    // Logs metrics to console
    // In production, send to Prometheus/CloudWatch/etc.
}
```

**What happens:**
- All metrics flow through this emitter
- You can implement any backend (Prometheus, StatsD, CloudWatch)
- Only need to configure the endpoint

## Running the Example

### Prerequisites

1. Install all framework dependencies to local Maven repository:
   ```bash
   cd ../instrumentation && mvn install
   cd ../r4j-wrapper && mvn install
   cd ../webclient-wrapper && mvn install
   cd ../network-executor-api && mvn install
   cd ../network-client-api && mvn install
   ```

2. Build the example:
   ```bash
   cd ../example
   mvn clean compile
   ```

### Run the Application

```bash
mvn spring-boot:run
```

### Test Endpoints

```bash
# Get a user (calls external service)
curl http://localhost:8080/api/example/users/1

# Get all users (database operation)
curl http://localhost:8080/api/example/users

# Risky operation (success)
curl http://localhost:8080/api/example/risky?fail=false

# Risky operation (failure - demonstrates error metrics)
curl http://localhost:8080/api/example/risky?fail=true
```

## What You'll See

### Console Output

You'll see metrics logged like:
```
📊 COUNTER: instrumented.latency = 1 (tags: {operation=processUser, service=example-service})
⏱️  LATENCY: instrumented.latency = 150ms (tags: {method=getUser, class=UserServiceImpl, status=success})
```

### Generated Code

After compilation, check:
- `target/generated-sources/annotations/com/ravi9a2/example/service/UserServiceImpl.java`

This shows the generated implementation with:
- HTTP client calls
- Circuit breaker protection
- Bulkhead isolation
- Automatic instrumentation

## Configuration

All configuration is in `application.properties`:

- **Downstream Services**: Configure base URLs, timeouts, connection pools
- **Circuit Breakers**: Failure thresholds, wait durations
- **Bulkheads**: Max concurrent calls
- **Metrics**: Enable/disable, endpoint configuration

## Next Steps

1. **Implement Real Metrics**: Replace `ExampleMetricEmitter` with Prometheus/CloudWatch implementation
2. **Add More Services**: Create more `@ExternalService` interfaces
3. **Configure Monitoring**: Set up dashboards using emitted metrics
4. **Add Retry**: Implement retry in executors (currently annotation exists but not implemented)

## Troubleshooting

### Compilation Errors

If you see annotation processor errors:
- Ensure all framework modules are installed to local Maven repository
- Check Java version compatibility (requires Java 8+)
- Try: `mvn clean install -U` to force dependency updates

### Missing Generated Code

If `UserServiceImpl` is not generated:
- Check that `r4j-wrapper` is in annotation processor paths
- Verify `@ExternalService` annotation is present
- Check `target/generated-sources/annotations/` directory

### Metrics Not Appearing

- Verify `ExampleMetricEmitter` bean is created
- Check `instrumentation.metrics.enabled=true` in properties
- Enable DEBUG logging: `logging.level.com.ravi9a2=DEBUG`

