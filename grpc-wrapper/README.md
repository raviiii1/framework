# gRPC Wrapper

A wrapper library that implements `RPCClient` interface for gRPC communication, providing integration with the network framework's executor pattern.

## Overview

The `grpc-wrapper` library provides:
- `GrpcClientWrapper`: Implementation of `RPCClient<ManagedChannel>` for gRPC calls
- `GrpcClientWrapperBuilder`: Builder for creating gRPC client instances
- `GrpcClientWrapperBeanLoader`: Spring configuration for automatic client registration

## Features

- **Dynamic gRPC Calls**: Uses reflection to dynamically invoke gRPC service methods
- **Unary RPC Support**: Full support for unary (request-response) gRPC calls
- **Async and Blocking**: Supports both `ListenableFuture` (async) and blocking calls
- **Spring Integration**: Automatic configuration via Spring Boot properties
- **Circuit Breaker Ready**: Works seamlessly with R4J and Hystrix executors

## Dependencies

- gRPC Java libraries (grpc-netty-shaded, grpc-protobuf, grpc-stub)
- Google Guava (for ListenableFuture)
- Spring Boot Starter
- network-client-api

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.ravi9a2</groupId>
    <artifactId>grpc-wrapper</artifactId>
    <version>0.0.1-RELEASE</version>
</dependency>
```

### 2. Configure in application.properties

```properties
# gRPC Client Configuration
downstream.rpc.clients.order-service.target=localhost:50051
downstream.rpc.clients.order-service.timeout-seconds=30
downstream.rpc.clients.order-service.use-plaintext=true

downstream.rpc.clients.user-service.target=localhost:50052
downstream.rpc.clients.user-service.timeout-seconds=30
downstream.rpc.clients.user-service.use-plaintext=false
```

### 3. Use with Generated Services

The annotation processor will automatically generate service implementations that use `GrpcClientWrapper`:

```java
@ExternalService
public interface OrderService {
    @UnaryRPCCall(
        service = "order-service",
        fqPackageName = "com.ravi9a2.example.proto",
        className = "OrderService",
        methodName = "GetOrder",
        rpcMethod = RPCMethod.UNARY
    )
    User getOrder(@PathParam("orderId") String orderId);
}
```

### 4. Direct Usage

```java
@Autowired
private RPCClientRegistry rpcClientRegistry;

@Autowired
private RPCExecutor<ManagedChannel> rpcExecutor;

public void makeGrpcCall() {
    GrpcClientWrapper client = (GrpcClientWrapper) rpcClientRegistry.client("order-service");
    
    RpcRequestSpec requestSpec = RpcRequestSpec.builder()
        .fqPackageName("com.ravi9a2.example.proto")
        .serviceName("OrderService")
        .methodName("GetOrder")
        .rpcMethod("UNARY")
        .body(requestMessage)
        .build();
    
    User response = client.blockingUnary(requestSpec);
}
```

## Configuration Properties

| Property | Description | Required | Default |
|----------|-------------|----------|---------|
| `downstream.rpc.clients.{service-name}.target` | gRPC server target (host:port) | Yes | - |
| `downstream.rpc.clients.{service-name}.timeout-seconds` | Request timeout in seconds | No | 30 |
| `downstream.rpc.clients.{service-name}.use-plaintext` | Use plaintext (no TLS) | No | false |

## How It Works

### Dynamic Service Invocation

The `GrpcClientWrapper` uses reflection to dynamically invoke gRPC service methods:

1. **Load Stub Class**: Loads the generated gRPC stub class (e.g., `OrderServiceGrpc`)
2. **Get Method Descriptor**: Retrieves the method descriptor using reflection
3. **Create Stub**: Creates a new stub instance using `newStub(channel)`
4. **Execute Call**: Uses `ClientCalls.futureUnaryCall()` to execute the gRPC call

### Expected gRPC Code Structure

The wrapper expects gRPC services to follow the standard protobuf/gRPC code generation pattern:

```
com.ravi9a2.example.proto.OrderServiceGrpc
  ├── newStub(ManagedChannel) -> AbstractStub
  ├── getGetOrderMethod() -> MethodDescriptor
  └── ...
```

### Request/Response Types

- **Request Body**: Must be a protobuf `Message` instance
- **Response Type**: Specified in `RpcRequestSpec.type()` for deserialization

## Limitations

### Currently Supported
- ✅ Unary RPC calls (request-response)
- ✅ Blocking calls
- ✅ Async calls (ListenableFuture)

### Not Yet Implemented
- ❌ Server streaming
- ❌ Client streaming
- ❌ Bidirectional streaming

## Integration with Executors

The `GrpcClientWrapper` works seamlessly with both R4J and Hystrix executors:

### R4J Executor
```java
@Autowired
private R4JRPCExecutor<ManagedChannel> rpcExecutor;

User user = rpcExecutor.execute(grpcClient, rpcCallDefinition);
```

### Hystrix Executor
```java
@Autowired
private HystrixRPCExecutor<ManagedChannel> rpcExecutor;

User user = rpcExecutor.execute(grpcClient, rpcCallDefinition);
```

## Error Handling

The wrapper throws `NetworkClientException` for:
- Invalid configuration (missing target, etc.)
- gRPC call failures
- Timeout errors
- Reflection errors (missing classes, methods, etc.)

## Example

See the `example` project for a complete working example using `OrderService` with gRPC.

## Notes

- The wrapper requires generated gRPC stub classes to be on the classpath
- Ensure protobuf definitions are compiled and available
- For production use, consider implementing proper protobuf message conversion for non-Message request bodies
- Streaming RPC support can be added as needed

