# Hystrix Wrapper

This module implements the `network-executor-api` interfaces using Netflix Hystrix for circuit breaker and thread pool isolation.

## Overview

Similar to `r4j-wrapper` which uses Resilience4j, `hystrix-wrapper` provides an alternative implementation using Netflix Hystrix. It implements:

- `ReactiveExecutor` - For reactive (Mono/Flux) calls
- `NonReactiveExecutor` - For blocking calls
- `RPCExecutor` - For gRPC calls

## Features

- **Circuit Breaker**: Automatic failure detection and circuit breaking
- **Thread Pool Isolation**: Isolated thread pools for bulkhead pattern
- **Fallback Support**: Graceful degradation when circuit is open
- **Reactive Support**: Full support for Reactor Mono/Flux via RxJava bridge

## Dependencies

- `com.netflix.hystrix:hystrix-core` - Core Hystrix library
- `io.reactivex:rxjava` - RxJava for reactive support
- `io.reactivex:rxjava-reactive-streams` - Bridge between RxJava and Reactive Streams
- `com.google.guava:guava` - For ListenableFuture support

## Usage

### Reactive Executor

```java
@Autowired
private ReactiveExecutor<WebClient> reactiveExecutor;

Mono<User> user = reactiveExecutor.executeToMono(webClient, callDefinition);
```

### Non-Reactive Executor

```java
@Autowired
private NonReactiveExecutor<HttpClient> nonReactiveExecutor;

User user = nonReactiveExecutor.execute(httpClient, callDefinition);
```

### RPC Executor

```java
@Autowired
private RPCExecutor<GrpcClientWrapper> rpcExecutor;

User user = rpcExecutor.execute(grpcClient, callDefinition);
```

## Configuration

Hystrix configuration is loaded from `application.properties` similar to R4J wrapper. Configuration is done via properties files:

### Circuit Breaker Configuration

```properties
# Default circuit breaker configuration
hystrix.circuit-breaker.default.circuitBreakerEnabled=true
hystrix.circuit-breaker.default.circuitBreakerRequestVolumeThreshold=20
hystrix.circuit-breaker.default.circuitBreakerSleepWindowInMilliseconds=5000
hystrix.circuit-breaker.default.circuitBreakerErrorThresholdPercentage=50
hystrix.circuit-breaker.default.executionTimeoutInMilliseconds=10000
hystrix.circuit-breaker.default.executionIsolationThreadTimeoutInMilliseconds=10000
hystrix.circuit-breaker.default.executionIsolationStrategy=THREAD

# Service-specific circuit breaker configuration
hystrix.circuit-breaker.user-service-cb.circuitBreakerRequestVolumeThreshold=10
hystrix.circuit-breaker.user-service-cb.circuitBreakerErrorThresholdPercentage=30
```

### Thread Pool Configuration

```properties
# Default thread pool configuration
hystrix.thread-pool.default.coreSize=10
hystrix.thread-pool.default.maximumSize=10
hystrix.thread-pool.default.maxQueueSize=-1
hystrix.thread-pool.default.keepAliveTimeMinutes=1
hystrix.thread-pool.default.queueSizeRejectionThreshold=5

# Service-specific thread pool configuration
hystrix.thread-pool.user-service-bh.coreSize=20
hystrix.thread-pool.user-service-bh.maximumSize=20
```

### Configuration Properties

**Circuit Breaker:**
- `circuitBreakerEnabled` - Enable/disable circuit breaker (default: `true`)
- `circuitBreakerRequestVolumeThreshold` - Minimum requests before circuit opens (default: `20`)
- `circuitBreakerSleepWindowInMilliseconds` - Time before retry (default: `5000`)
- `circuitBreakerErrorThresholdPercentage` - Error percentage threshold (default: `50`)
- `executionTimeoutInMilliseconds` - Command execution timeout (default: `10000`)
- `executionIsolationThreadTimeoutInMilliseconds` - Thread timeout (default: `10000`)
- `executionIsolationStrategy` - THREAD or SEMAPHORE (default: `THREAD`)

**Thread Pool:**
- `coreSize` - Core thread pool size (default: `10`)
- `maximumSize` - Maximum thread pool size (default: `10`)
- `maxQueueSize` - Maximum queue size, -1 for unbounded (default: `-1`)
- `keepAliveTimeMinutes` - Thread keep-alive time (default: `1`)
- `queueSizeRejectionThreshold` - Queue rejection threshold (default: `5`)

## Comparison with R4J Wrapper

| Feature | Hystrix Wrapper | R4J Wrapper |
|---------|----------------|-------------|
| Circuit Breaker | ✅ | ✅ |
| Bulkhead | Thread Pool | Semaphore/Thread Pool |
| Reactive Support | Via RxJava bridge | Native Reactor |
| Configuration | HystrixCommand properties | Resilience4j config |
| Status | Maintenance Mode | Active Development |

## Notes

- Netflix Hystrix is in maintenance mode. For new projects, consider using Resilience4j (via `r4j-wrapper`)
- This wrapper is provided as an alternative for existing Hystrix-based systems
- The reactive executor uses RxJava bridge to convert between Reactor and RxJava types

