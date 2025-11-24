# Network Framework

A comprehensive, multi-module Java framework for building resilient, observable, and maintainable network clients. The framework provides abstractions for HTTP (reactive and blocking), gRPC, circuit breakers, bulkheads, and metrics collection.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Modules](#modules)
  - [network-client-api](#network-client-api)
  - [network-executor-api](#network-executor-api)
  - [webclient-wrapper](#webclient-wrapper)
  - [httpclient-wrapper](#httpclient-wrapper)
  - [grpc-wrapper](#grpc-wrapper)
  - [r4j-wrapper](#r4j-wrapper)
  - [hysterix-wrapper](#hysterix-wrapper)
  - [intstrumentation](#intstrumentation)
- [Quick Start](#quick-start)
- [Configuration Examples](#configuration-examples)
- [Best Practices](#best-practices)

## Overview

The Network Framework is designed to simplify building resilient microservices by providing:

- **Unified Client APIs**: Consistent interfaces for reactive (WebClient), blocking (HttpClient), and gRPC clients
- **Code Generation**: Automatic implementation generation from annotated service interfaces
- **Resilience Patterns**: Built-in circuit breakers and bulkheads via Resilience4j or Hystrix
- **Observability**: Automatic metrics collection with pluggable backends
- **Spring Integration**: Seamless Spring Boot integration with auto-configuration

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Your Application                         │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  @ExternalService Interfaces (Code Generated)        │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              network-executor-api                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │ Reactive     │  │ NonReactive  │  │ RPC          │    │
│  │ Executor     │  │ Executor     │  │ Executor     │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ r4j-wrapper  │  │hystrix-wrapper│  │  (Future)    │
│ (Resilience) │  │  (Hystrix)   │  │  Retry, etc. │
└──────────────┘  └──────────────┘  └──────────────┘
        │                   │
        └───────────────────┘
                    │
        ┌───────────┼───────────┐
        ▼           ▼           ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│webclient-    │ │httpclient-   │ │grpc-wrapper  │
│wrapper       │ │wrapper       │ │              │
└──────────────┘ └──────────────┘ └──────────────┘
        │               │               │
        └───────────────┼───────────────┘
                        ▼
            ┌───────────────────────┐
            │ network-client-api    │
            │ (Core Abstractions)   │
            └───────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Spring       │ │ Apache        │ │ gRPC         │
│ WebClient    │ │ HttpClient    │ │ ManagedChannel│
└──────────────┘ └──────────────┘ └──────────────┘
```

## Modules

### network-client-api

**Purpose**: Core abstractions for network clients. Defines interfaces for reactive, non-reactive, and RPC clients.

**Key Interfaces**:
- `ReactiveClient<C>`: For reactive clients (returns `Mono<T>`/`Flux<T>`)
- `NonReactiveClient<C>`: For blocking clients (returns `T`)
- `RPCClient<C>`: For gRPC clients (returns `T` or `ListenableFuture<T>`)
- `ReactiveClientRegistry`, `NonReactiveClientRegistry`, `RPCClientRegistry`: Client registries

**Usage Example**:

```java
// Get a reactive client from registry
@Autowired
private ReactiveClientRegistry reactiveClientRegistry;

ReactiveClient<WebClient> userServiceClient = 
    reactiveClientRegistry.client("user-service");

// Make a reactive call
Mono<User> user = userServiceClient.getToMono(
    RestRequestSpec.builder()
        .path("/api/users/{userId}")
        .pathParams(Map.of("userId", "123"))
        .build()
);
```

**Configuration**: No direct configuration needed. Used by wrapper modules.

---

### network-executor-api

**Purpose**: Defines executor interfaces that wrap clients and provide resilience patterns (circuit breakers, bulkheads).

**Key Interfaces**:
- `ReactiveExecutor<C>`: Executes reactive calls with resilience
- `NonReactiveExecutor<C>`: Executes blocking calls with resilience
- `RPCExecutor<C>`: Executes gRPC calls with resilience

**Annotations for Code Generation**:
- `@ExternalService`: Marks an interface for code generation
- `@GetCall`, `@PostCall`, `@PutCall`, `@DeleteCall`, `@PatchCall`: HTTP method annotations
- `@UnaryRPCCall`, `@BidiRPCCall`, `@ClientStreamRPCCall`, `@ServiceStreamRPCCall`: gRPC annotations
- `@PathParam`, `@QueryParam`, `@Header`, `@Headers`, `@Payload`: Parameter annotations

**Usage Example**:

```java
@ExternalService
public interface UserService {
    
    @GetCall(
        path = "/api/users/{userId}",
        service = "user-service",
        cbEnabled = true,
        circuitBreaker = "user-service-cb",
        bhEnabled = true,
        bulkhead = "user-service-bh"
    )
    Mono<User> getUser(@PathParam("userId") String userId);
    
    @PostCall(
        path = "/api/users",
        service = "user-service",
        cbEnabled = true,
        circuitBreaker = "user-service-cb"
    )
    Mono<User> createUser(@Payload User user);
}
```

The annotation processor automatically generates `UserServiceImpl` with:
- HTTP client calls
- Circuit breaker protection
- Bulkhead isolation
- Automatic instrumentation

**Configuration**: No direct configuration. Configured through wrapper modules (r4j-wrapper or hysterix-wrapper).

---

### webclient-wrapper

**Purpose**: Implements `ReactiveClient<WebClient>` using Spring WebClient for reactive HTTP calls.

**Features**:
- Non-blocking, reactive HTTP calls
- Connection pooling
- Configurable timeouts
- Automatic error handling
- Spring Boot auto-configuration

**Usage Example**:

```java
@Autowired
private ReactiveClientRegistry reactiveClientRegistry;

@Autowired
private ReactiveExecutor<WebClient> reactiveExecutor;

public Mono<User> getUser(String userId) {
    ReactiveClient<WebClient> client = 
        reactiveClientRegistry.client("user-service");
    
    RestCallDefinition callDef = RestCallDefinition.builder()
        .path("/api/users/{userId}")
        .pathParams(Map.of("userId", userId))
        .build();
    
    return reactiveExecutor.executeToMono(client, callDef);
}
```

**Configuration Example** (`application.properties`):

```properties
# WebClient Configuration
downstream.user-service.baseUrl=https://api.example.com
downstream.user-service.maxConnections=100
downstream.user-service.connectTimeout=5000
downstream.user-service.readTimeout=10000
downstream.user-service.writeTimeout=10000
downstream.user-service.inMemoryBufferSizeInKB=256

# Optional: Separate thread pool for WebClient
webClient.tp.enableSeparateTP=false
webClient.tp.corePoolSize=1
webClient.tp.maxPoolSize=200
webClient.tp.queueCapacity=65556
webClient.tp.threadNamePrefix=WebClient-TP-

# Codec configuration
webClient.codec.inMemoryBufferSizeInKB=256

# Default configuration (applies to all services if not overridden)
downstream.default.maxConnections=50
downstream.default.connectTimeout=5000
downstream.default.readTimeout=10000
downstream.default.writeTimeout=10000
```

**Maven Dependency**:

```xml
<dependency>
    <groupId>com.ravi9a2</groupId>
    <artifactId>webclient-wrapper</artifactId>
    <version>0.0.1-RELEASE</version>
</dependency>
```

---

### httpclient-wrapper

**Purpose**: Implements `NonReactiveClient<HttpClient>` using Apache HttpClient for blocking HTTP calls.

**Features**:
- Blocking, synchronous HTTP calls
- Connection pooling
- Configurable timeouts
- Automatic error handling
- Spring Boot auto-configuration

**Usage Example**:

```java
@Autowired
private NonReactiveClientRegistry nonReactiveClientRegistry;

@Autowired
private NonReactiveExecutor<HttpClient> nonReactiveExecutor;

public User getUser(String userId) {
    NonReactiveClient<HttpClient> client = 
        nonReactiveClientRegistry.client("product-service");
    
    RestCallDefinition callDef = RestCallDefinition.builder()
        .path("/api/products/{productId}")
        .pathParams(Map.of("productId", userId))
        .build();
    
    return nonReactiveExecutor.execute(client, callDef);
}
```

**Configuration Example** (`application.properties`):

```properties
# HttpClient Configuration
downstream.product-service.baseUrl=https://api.example.com
downstream.product-service.maxConnections=50
downstream.product-service.defaultMaxPerRoute=20
downstream.product-service.connectTimeout=5000
downstream.product-service.readTimeout=10000
downstream.product-service.writeTimeout=10000
downstream.product-service.socketTimeout=10000

# Default configuration
downstream.default.maxConnections=50
downstream.default.defaultMaxPerRoute=20
downstream.default.connectTimeout=5000
downstream.default.readTimeout=10000
downstream.default.writeTimeout=10000
downstream.default.socketTimeout=10000
```

**Maven Dependency**:

```xml
<dependency>
    <groupId>com.ravi9a2</groupId>
    <artifactId>httpclient-wrapper</artifactId>
    <version>0.0.1-RELEASE</version>
</dependency>
```

---

### grpc-wrapper

**Purpose**: Implements `RPCClient<ManagedChannel>` for gRPC communication.

**Features**:
- Dynamic gRPC service invocation via reflection
- Unary RPC support (request-response)
- Async (`ListenableFuture`) and blocking calls
- Spring Boot auto-configuration
- Circuit breaker ready

**Usage Example**:

```java
@ExternalService
public interface OrderService {
    @UnaryRPCCall(
        service = "order-service",
        cbEnabled = true,
        circuitBreaker = "order-service-cb",
        fqPackageName = "com.example.proto",
        className = "OrderService",
        methodName = "GetOrder",
        rpcMethod = RPCMethod.UNARY
    )
    Order getOrder(@PathParam("orderId") String orderId);
}
```

**Direct Usage**:

```java
@Autowired
private RPCClientRegistry rpcClientRegistry;

@Autowired
private RPCExecutor<ManagedChannel> rpcExecutor;

public Order getOrder(String orderId) {
    RPCClient<ManagedChannel> client = 
        rpcClientRegistry.client("order-service");
    
    RpcRequestSpec requestSpec = RpcRequestSpec.builder()
        .fqPackageName("com.example.proto")
        .serviceName("OrderService")
        .methodName("GetOrder")
        .rpcMethod("UNARY")
        .body(requestMessage)
        .build();
    
    return client.blockingUnary(requestSpec);
}
```

**Configuration Example** (`application.properties`):

```properties
# gRPC Client Configuration
downstream.rpc.clients.order-service.target=localhost:50051
downstream.rpc.clients.order-service.timeout-seconds=30
downstream.rpc.clients.order-service.use-plaintext=true

downstream.rpc.clients.user-service.target=localhost:50052
downstream.rpc.clients.user-service.timeout-seconds=30
downstream.rpc.clients.user-service.use-plaintext=false
```

**Maven Dependency**:

```xml
<dependency>
    <groupId>com.ravi9a2</groupId>
    <artifactId>grpc-wrapper</artifactId>
    <version>0.0.1-RELEASE</version>
</dependency>
```

**Note**: Currently supports unary RPC calls. Server streaming, client streaming, and bidirectional streaming are planned.

---

### r4j-wrapper

**Purpose**: Implements executor interfaces using Resilience4j for circuit breakers and bulkheads.

**Features**:
- Circuit breaker pattern
- Bulkhead pattern (semaphore and thread pool)
- Native Reactor support
- Spring Boot auto-configuration
- Active development and maintenance

**Usage Example**:

```java
@Autowired
private R4JReactiveExecutor<WebClient> reactiveExecutor;

@Autowired
private R4JNonReactiveExecutor<HttpClient> nonReactiveExecutor;

@Autowired
private R4JRPCExecutor<ManagedChannel> rpcExecutor;

// Reactive execution
Mono<User> user = reactiveExecutor.executeToMono(client, callDefinition);

// Blocking execution
User user = nonReactiveExecutor.execute(client, callDefinition);

// gRPC execution
Order order = rpcExecutor.execute(grpcClient, rpcCallDefinition);
```

**Configuration Example** (`application.properties`):

```properties
# Circuit Breaker Configuration
resilience4j.circuitbreaker.configs.default.failureRateThreshold=50
resilience4j.circuitbreaker.configs.default.waitDurationInOpenState=10000
resilience4j.circuitbreaker.configs.default.slidingWindowSize=10
resilience4j.circuitbreaker.configs.default.minimumNumberOfCalls=5
resilience4j.circuitbreaker.configs.default.permittedNumberOfCallsInHalfOpenState=3
resilience4j.circuitbreaker.configs.default.automaticTransitionFromOpenToHalfOpenEnabled=false
resilience4j.circuitbreaker.configs.default.recordExceptions=java.io.IOException,java.util.concurrent.TimeoutException
resilience4j.circuitbreaker.configs.default.ignoreExceptions=

# Service-specific circuit breaker
resilience4j.circuitbreaker.instances.user-service-cb.baseConfig=default
resilience4j.circuitbreaker.instances.user-service-cb.failureRateThreshold=30

# Bulkhead Configuration (Semaphore)
resilience4j.bulkhead.configs.default.maxConcurrentCalls=10
resilience4j.bulkhead.instances.user-service-bh.baseConfig=default
resilience4j.bulkhead.instances.user-service-bh.maxConcurrentCalls=20

# Bulkhead Configuration (Thread Pool)
resilience4j.thread-pool-bulkhead.configs.default.coreThreadPoolSize=10
resilience4j.thread-pool-bulkhead.configs.default.maxThreadPoolSize=20
resilience4j.thread-pool-bulkhead.configs.default.queueCapacity=100
resilience4j.thread-pool-bulkhead.instances.user-service-tpbh.baseConfig=default
```

**Maven Dependency**:

```xml
<dependency>
    <groupId>com.ravi9a2</groupId>
    <artifactId>r4j-wrapper</artifactId>
    <version>0.0.1-RELEASE</version>
</dependency>
```

**Configuration Properties**:

| Property | Description | Default |
|----------|-------------|---------|
| `failureRateThreshold` | Failure rate percentage to open circuit | 50 |
| `waitDurationInOpenState` | Wait time before retry (ms) | 10000 |
| `slidingWindowSize` | Number of calls in sliding window | 10 |
| `minimumNumberOfCalls` | Minimum calls before circuit can open | 5 |
| `maxConcurrentCalls` | Maximum concurrent calls (bulkhead) | 10 |

---

### hysterix-wrapper

**Purpose**: Implements executor interfaces using Netflix Hystrix for circuit breakers and thread pool isolation.

**Features**:
- Circuit breaker pattern
- Thread pool isolation (bulkhead)
- Reactive support via RxJava bridge
- Spring Boot auto-configuration

**Note**: Netflix Hystrix is in maintenance mode. For new projects, prefer `r4j-wrapper`.

**Usage Example**:

```java
@Autowired
private HystrixReactiveExecutor<WebClient> reactiveExecutor;

@Autowired
private HystrixNonReactiveExecutor<HttpClient> nonReactiveExecutor;

@Autowired
private HystrixRPCExecutor<ManagedChannel> rpcExecutor;

// Reactive execution
Mono<User> user = reactiveExecutor.executeToMono(client, callDefinition);

// Blocking execution
User user = nonReactiveExecutor.execute(client, callDefinition);

// gRPC execution
Order order = rpcExecutor.execute(grpcClient, rpcCallDefinition);
```

**Configuration Example** (`application.properties`):

```properties
# Default Circuit Breaker Configuration
hystrix.circuit-breaker.default.circuitBreakerEnabled=true
hystrix.circuit-breaker.default.circuitBreakerRequestVolumeThreshold=20
hystrix.circuit-breaker.default.circuitBreakerSleepWindowInMilliseconds=5000
hystrix.circuit-breaker.default.circuitBreakerErrorThresholdPercentage=50
hystrix.circuit-breaker.default.executionTimeoutInMilliseconds=10000
hystrix.circuit-breaker.default.executionIsolationThreadTimeoutInMilliseconds=10000
hystrix.circuit-breaker.default.executionIsolationStrategy=THREAD

# Service-specific Circuit Breaker
hystrix.circuit-breaker.user-service-cb.circuitBreakerRequestVolumeThreshold=10
hystrix.circuit-breaker.user-service-cb.circuitBreakerErrorThresholdPercentage=30

# Default Thread Pool Configuration
hystrix.thread-pool.default.coreSize=10
hystrix.thread-pool.default.maximumSize=10
hystrix.thread-pool.default.maxQueueSize=-1
hystrix.thread-pool.default.keepAliveTimeMinutes=1
hystrix.thread-pool.default.queueSizeRejectionThreshold=5

# Service-specific Thread Pool
hystrix.thread-pool.user-service-bh.coreSize=20
hystrix.thread-pool.user-service-bh.maximumSize=20
hystrix.thread-pool.user-service-bh.maxQueueSize=100
```

**Maven Dependency**:

```xml
<dependency>
    <groupId>com.ravi9a2</groupId>
    <artifactId>hystrix-wrapper</artifactId>
    <version>0.0.1-RELEASE</version>
</dependency>
```

**Configuration Properties**:

| Property | Description | Default |
|----------|-------------|---------|
| `circuitBreakerEnabled` | Enable/disable circuit breaker | true |
| `circuitBreakerRequestVolumeThreshold` | Min requests before circuit opens | 20 |
| `circuitBreakerSleepWindowInMilliseconds` | Time before retry (ms) | 5000 |
| `circuitBreakerErrorThresholdPercentage` | Error percentage threshold | 50 |
| `executionTimeoutInMilliseconds` | Command execution timeout | 10000 |
| `coreSize` | Core thread pool size | 10 |
| `maximumSize` | Maximum thread pool size | 10 |
| `maxQueueSize` | Maximum queue size (-1 = unbounded) | -1 |

---

### intstrumentation

**Purpose**: Provides automatic metrics collection through annotations.

**Features**:
- `@Instrumented` annotation for automatic metric collection
- AspectJ-based interception
- Pluggable metric emitters (Prometheus, CloudWatch, StatsD, etc.)
- Spring Boot auto-configuration

**Usage Example**:

```java
@Service
public class UserService {
    
    @Instrumented(
        metricType = MetricType.HTTP,
        tagSet = "operation=getUser,service=user-service"
    )
    public Mono<User> getUser(String userId) {
        // Your implementation
        // Metrics are automatically collected
    }
}
```

**Custom Metric Emitter**:

```java
@Component
public class PrometheusMetricEmitter implements MetricEmitter {
    
    private final Counter.Builder counterBuilder;
    private final Histogram.Builder histogramBuilder;
    
    public PrometheusMetricEmitter() {
        this.counterBuilder = Counter.build()
            .name("instrumented_requests_total")
            .help("Total instrumented requests");
        this.histogramBuilder = Histogram.build()
            .name("instrumented_latency_seconds")
            .help("Instrumented method latency");
    }
    
    @Override
    public void incrementCounter(String metricName, double value, Map<String, String> tags) {
        Counter counter = counterBuilder.labelNames(tags.keySet().toArray(new String[0]))
            .register();
        counter.labels(tags.values().toArray(new String[0])).inc(value);
    }
    
    @Override
    public void recordLatency(String metricName, long latency, Map<String, String> tags) {
        Histogram histogram = histogramBuilder.labelNames(tags.keySet().toArray(new String[0]))
            .register();
        histogram.labels(tags.values().toArray(new String[0]))
            .observe(latency / 1000.0); // Convert ms to seconds
    }
    
    // Implement other methods...
}
```

**Configuration Example** (`application.properties`):

```properties
# Instrumentation Configuration
instrumentation.metrics.enabled=true
instrumentation.metrics.endpoint=http://localhost:9091/metrics
```

**Maven Dependency**:

```xml
<dependency>
    <groupId>com.ravi9a2</groupId>
    <artifactId>instrumentation</artifactId>
    <version>0.0.1-RELEASE</version>
</dependency>
```

**Metrics Collected**:

- `instrumented.latency`: Method execution time (histogram)
- `instrumented.success`: Count of successful invocations (counter)
- `instrumented.failure`: Count of failed invocations (counter)

All metrics include tags:
- `method`: Method name
- `class`: Class simple name
- `metricType`: Metric type (HTTP, RPC, etc.)
- `exception`: Exception class name (on failure)
- Custom tags from `tagSet` parameter

---

## Quick Start

### 1. Add Dependencies

```xml
<dependencies>
    <!-- Core APIs -->
    <dependency>
        <groupId>com.ravi9a2</groupId>
        <artifactId>network-client-api</artifactId>
        <version>0.0.1-RELEASE</version>
    </dependency>
    <dependency>
        <groupId>com.ravi9a2</groupId>
        <artifactId>network-executor-api</artifactId>
        <version>0.0.1-RELEASE</version>
    </dependency>
    
    <!-- Client Wrappers (choose based on your needs) -->
    <dependency>
        <groupId>com.ravi9a2</groupId>
        <artifactId>webclient-wrapper</artifactId>
        <version>0.0.1-RELEASE</version>
    </dependency>
    <!-- OR -->
    <dependency>
        <groupId>com.ravi9a2</groupId>
        <artifactId>httpclient-wrapper</artifactId>
        <version>0.0.1-RELEASE</version>
    </dependency>
    
    <!-- Resilience (choose one) -->
    <dependency>
        <groupId>com.ravi9a2</groupId>
        <artifactId>r4j-wrapper</artifactId>
        <version>0.0.1-RELEASE</version>
    </dependency>
    <!-- OR -->
    <dependency>
        <groupId>com.ravi9a2</groupId>
        <artifactId>hystrix-wrapper</artifactId>
        <version>0.0.1-RELEASE</version>
    </dependency>
    
    <!-- Instrumentation -->
    <dependency>
        <groupId>com.ravi9a2</groupId>
        <artifactId>instrumentation</artifactId>
        <version>0.0.1-RELEASE</version>
    </dependency>
</dependencies>
```

### 2. Configure Services

```properties
# application.properties
downstream.user-service.baseUrl=https://api.example.com
downstream.user-service.maxConnections=100
downstream.user-service.connectTimeout=5000
downstream.user-service.readTimeout=10000

# Circuit Breaker
resilience4j.circuitbreaker.instances.user-service-cb.baseConfig=default

# Bulkhead
resilience4j.bulkhead.instances.user-service-bh.baseConfig=default
```

### 3. Create Service Interface

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

### 4. Use the Service

```java
@Service
public class MyService {
    @Autowired
    private UserService userService; // Auto-generated implementation
    
    public void doSomething() {
        Mono<User> user = userService.getUser("123");
        // Use the user...
    }
}
```

That's it! The annotation processor generates the implementation automatically.

---

## Configuration Examples

### Complete Configuration Example

```properties
# Server Configuration
server.port=8080

# Instrumentation
instrumentation.metrics.enabled=true
instrumentation.metrics.endpoint=http://localhost:9091/metrics

# Reactive Service (WebClient)
downstream.user-service.baseUrl=https://jsonplaceholder.typicode.com
downstream.user-service.maxConnections=100
downstream.user-service.connectTimeout=5000
downstream.user-service.readTimeout=10000
downstream.user-service.writeTimeout=10000

# Non-Reactive Service (HttpClient)
downstream.product-service.baseUrl=https://jsonplaceholder.typicode.com
downstream.product-service.maxConnections=50
downstream.product-service.connectTimeout=5000
downstream.product-service.readTimeout=10000

# gRPC Service
downstream.rpc.clients.order-service.target=localhost:50051
downstream.rpc.clients.order-service.timeout-seconds=30
downstream.rpc.clients.order-service.use-plaintext=true

# Default Configuration
downstream.default.maxConnections=50
downstream.default.connectTimeout=5000
downstream.default.readTimeout=10000

# Resilience4j Circuit Breaker
resilience4j.circuitbreaker.configs.default.failureRateThreshold=50
resilience4j.circuitbreaker.configs.default.waitDurationInOpenState=10000
resilience4j.circuitbreaker.configs.default.slidingWindowSize=10
resilience4j.circuitbreaker.configs.default.minimumNumberOfCalls=5

resilience4j.circuitbreaker.instances.user-service-cb.baseConfig=default
resilience4j.circuitbreaker.instances.product-service-cb.baseConfig=default

# Resilience4j Bulkhead
resilience4j.bulkhead.configs.default.maxConcurrentCalls=10
resilience4j.bulkhead.instances.user-service-bh.baseConfig=default
resilience4j.bulkhead.instances.product-service-bh.baseConfig=default
```

### Authentication Configuration

```properties
# For services requiring authentication
downstream.user-service.baseUrl=https://api.example.com
downstream.user-service.authKey=Authorization
downstream.user-service.authSecret=Bearer your-token-here
downstream.user-service.secondAuthKey=X-API-Key
downstream.user-service.secondAuthSecret=your-api-key
```

---

## Best Practices

### 1. Service Interface Design

- Use `@ExternalService` on interfaces, not implementations
- Return `Mono<T>` or `Flux<T>` for reactive services
- Return `T` for blocking services
- Use descriptive service names in `@GetCall`, `@PostCall`, etc.

```java
@ExternalService
public interface PaymentService {
    @PostCall(
        path = "/api/payments",
        service = "payment-service",  // Use consistent naming
        cbEnabled = true,
        circuitBreaker = "payment-service-cb"
    )
    Mono<Payment> processPayment(@Payload PaymentRequest request);
}
```

### 2. Circuit Breaker Configuration

- Set appropriate failure thresholds based on your service's reliability
- Use different circuit breakers for different services
- Monitor circuit breaker metrics

```properties
# Critical service - lower threshold
resilience4j.circuitbreaker.instances.payment-service-cb.failureRateThreshold=20

# Non-critical service - higher threshold
resilience4j.circuitbreaker.instances.logging-service-cb.failureRateThreshold=50
```

### 3. Bulkhead Configuration

- Use bulkheads to prevent cascading failures
- Set `maxConcurrentCalls` based on downstream service capacity
- Use thread pool bulkheads for CPU-intensive operations

```properties
# High-throughput service
resilience4j.bulkhead.instances.user-service-bh.maxConcurrentCalls=50

# Low-throughput service
resilience4j.bulkhead.instances.admin-service-bh.maxConcurrentCalls=5
```

### 4. Metrics Collection

- Implement a custom `MetricEmitter` for your metrics backend
- Use meaningful tag names
- Monitor latency percentiles (p50, p95, p99)

```java
@Instrumented(
    metricType = MetricType.HTTP,
    tagSet = "operation=getUser,service=user-service,version=v1"
)
public Mono<User> getUser(String userId) {
    // Implementation
}
```

### 5. Error Handling

- Use appropriate exception types from `network-client-api`
- Implement fallback logic when circuit breakers are open
- Log errors with context

```java
@GetCall(
    path = "/api/users/{userId}",
    service = "user-service",
    cbEnabled = true,
    circuitBreaker = "user-service-cb"
)
Mono<User> getUser(@PathParam("userId") String userId);

// In your service
public Mono<User> getUserWithFallback(String userId) {
    return userService.getUser(userId)
        .onErrorResume(e -> {
            log.error("Failed to get user: {}", userId, e);
            return Mono.just(getDefaultUser());
        });
}
```

### 6. Timeout Configuration

- Set timeouts based on downstream service SLAs
- Use different timeouts for different operations
- Consider network latency in timeout calculations

```properties
# Fast service
downstream.user-service.readTimeout=2000

# Slow service
downstream.report-service.readTimeout=30000
```

---

## Building the Framework

### Build All Modules

```bash
mvn clean install
```

### Build Specific Module

```bash
mvn clean install -pl network-client-api
```

### Build with Dependencies

```bash
mvn clean install -pl example -am
```

---

## Examples

See the `example` module for complete working examples:

- Reactive service with WebClient
- Non-reactive service with HttpClient
- gRPC service
- Circuit breaker and bulkhead configuration
- Metrics collection
- Custom metric emitters

Run the example:

```bash
cd example
mvn spring-boot:run
```

---

## License

[[License](https://github.com/raviiii1/framework/blob/main/LICENSE)]

---

## Contributing

[Contributing Guidelines]

---

## Support

For issues and questions, please open an issue on GitHub.

