package com.ravi9a2.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Message;
import com.ravi9a2.nca.RPCClient;
import com.ravi9a2.nca.data.RpcRequestSpec;
import com.ravi9a2.nca.exceptions.NetworkClientException;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.ClientCalls;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * gRPC client wrapper that implements RPCClient interface.
 * This wrapper provides dynamic gRPC call capabilities using reflection
 * to invoke gRPC service methods based on RpcRequestSpec.
 * 
 * @author raviprakash
 */
@Slf4j
public class GrpcClientWrapper implements RPCClient<ManagedChannel> {

    private final ManagedChannel channel;
    private final long timeoutSeconds;

    public GrpcClientWrapper(ManagedChannel channel) {
        this(channel, 30);
    }

    public GrpcClientWrapper(ManagedChannel channel, long timeoutSeconds) {
        Assert.notNull(channel, "ManagedChannel cannot be null");
        this.channel = channel;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public <R> ListenableFuture<R> future(RpcRequestSpec requestSpec) {
        return futureUnary(requestSpec);
    }

    @Override
    public <R> ListenableFuture<R> futureUnary(RpcRequestSpec requestSpec) {
        try {
            return executeUnaryCall(requestSpec, true);
        } catch (Exception e) {
            log.error("Error executing unary gRPC call: {}", e.getMessage(), e);
            throw new NetworkClientException(e);
        }
    }

    @Override
    public <R> ListenableFuture<R> futureBiDi(RpcRequestSpec requestSpec) {
        // Bidirectional streaming not yet implemented
        throw new UnsupportedOperationException("Bidirectional streaming not yet implemented");
    }

    @Override
    public <R> ListenableFuture<R> futureServerStreaming(RpcRequestSpec requestSpec) {
        // Server streaming not yet implemented
        throw new UnsupportedOperationException("Server streaming not yet implemented");
    }

    @Override
    public <R> ListenableFuture<R> futureClientStreaming(RpcRequestSpec requestSpec) {
        // Client streaming not yet implemented
        throw new UnsupportedOperationException("Client streaming not yet implemented");
    }

    @Override
    public <R> R blocking(RpcRequestSpec requestSpec) {
        return blockingUnary(requestSpec);
    }

    @Override
    public <R> R blockingUnary(RpcRequestSpec requestSpec) {
        try {
            ListenableFuture<R> future = executeUnaryCall(requestSpec, false);
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            log.error("Error executing blocking unary gRPC call: {}", e.getMessage(), e);
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new NetworkClientException(cause != null ? cause : e);
        } catch (Exception e) {
            log.error("Error executing blocking unary gRPC call: {}", e.getMessage(), e);
            throw new NetworkClientException(e);
        }
    }

    @Override
    public <R> R blockingBiDi(RpcRequestSpec requestSpec) {
        throw new UnsupportedOperationException("Bidirectional streaming not yet implemented");
    }

    @Override
    public <R> R blockingServerStreaming(RpcRequestSpec requestSpec) {
        throw new UnsupportedOperationException("Server streaming not yet implemented");
    }

    @Override
    public <R> R blockingClientStreaming(RpcRequestSpec requestSpec) {
        throw new UnsupportedOperationException("Client streaming not yet implemented");
    }

    /**
     * Executes a unary gRPC call using reflection to dynamically invoke the service
     * method.
     * 
     * @param requestSpec The RPC request specification
     * @param async       Whether to return a future or block
     * @return The result or a future
     */
    @SuppressWarnings("unchecked")
    private <R> ListenableFuture<R> executeUnaryCall(RpcRequestSpec requestSpec, boolean async) {
        try {
            String fqPackageName = requestSpec.getFqPackageName();
            String serviceName = requestSpec.getServiceName();
            String methodName = requestSpec.getMethodName();

            Assert.notNull(fqPackageName, "Fully qualified package name cannot be null");
            Assert.notNull(serviceName, "Service name cannot be null");
            Assert.notNull(methodName, "Method name cannot be null");

            // Load the service stub class using reflection
            // Expected pattern: com.ravi9a2.example.proto.OrderServiceGrpc
            String stubClassName = fqPackageName + "." + serviceName + "Grpc";
            Class<?> stubClass = Class.forName(stubClassName);

            // Get the newStub method (e.g., OrderServiceGrpc.newStub(channel))
            Method newStubMethod = stubClass.getMethod("newStub", ManagedChannel.class);
            AbstractStub<?> stub = (AbstractStub<?>) newStubMethod.invoke(null, channel);

            // Get the method descriptor
            // Expected pattern: OrderServiceGrpc.getGetOrderMethod()
            String methodDescriptorMethodName = "get" + methodName + "Method";
            Method getMethodDescriptor = stubClass.getMethod(methodDescriptorMethodName);
            MethodDescriptor<Message, Message> methodDescriptor = (MethodDescriptor<Message, Message>) getMethodDescriptor
                    .invoke(null);

            // Convert request body to protobuf message if needed
            Message request = convertToMessage(requestSpec.getBody(), methodDescriptor.getRequestMarshaller());

            // Execute the call
            if (async) {
                return (ListenableFuture<R>) ClientCalls.futureUnaryCall(
                        channel.newCall(methodDescriptor, stub.getCallOptions()),
                        request);
            } else {
                // For blocking, we'll use the async version and get() it
                ListenableFuture<Message> future = ClientCalls.futureUnaryCall(
                        channel.newCall(methodDescriptor, stub.getCallOptions()),
                        request);
                return (ListenableFuture<R>) future;
            }
        } catch (Exception e) {
            log.error("Error executing gRPC call for {}.{}.{}: {}",
                    requestSpec.getFqPackageName(),
                    requestSpec.getServiceName(),
                    requestSpec.getMethodName(),
                    e.getMessage(), e);
            throw new NetworkClientException(e);
        }
    }

    /**
     * Converts the request body to a protobuf Message.
     * If body is already a Message, returns it as-is.
     * Otherwise, attempts to convert using Jackson or other means.
     */
    @SuppressWarnings("unchecked")
    private Message convertToMessage(Object body, MethodDescriptor.Marshaller<Message> marshaller) {
        if (body == null) {
            // For empty requests, try to create an empty message
            // This is a simplified approach - in production, you'd need proper protobuf
            // message creation
            throw new IllegalArgumentException("Request body cannot be null for gRPC calls");
        }

        if (body instanceof Message) {
            return (Message) body;
        }

        // If body is not a Message, we need to convert it
        // This is a simplified implementation - in production, you'd use proper
        // protobuf builders
        throw new IllegalArgumentException(
                "Request body must be a protobuf Message instance. Got: " + body.getClass().getName());
    }

    /**
     * Shuts down the underlying channel.
     */
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown();
                if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                    channel.shutdownNow();
                }
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
