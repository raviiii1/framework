package com.ravi9a2.example.service;

import com.ravi9a2.example.model.User;
import com.ravi9a2.nea.annotations.ExternalService;
import com.ravi9a2.nea.annotations.UnaryRPCCall;
import com.ravi9a2.nea.annotations.PathParam;
import com.ravi9a2.nea.annotations.Payload;
import com.ravi9a2.nea.core.data.RPCMethod;

/**
 * gRPC service example.
 * 
 * This interface demonstrates gRPC unary calls.
 * Methods can return either blocking types or ListenableFuture for async.
 * 
 * Note: This requires a gRPC client wrapper implementation (GrpcClientWrapper).
 * The annotation processor will generate OrderServiceImpl with:
 * - gRPC client calls
 * - Circuit breaker protection
 * - Bulkhead isolation
 * - Automatic instrumentation
 */
@ExternalService
public interface OrderService {

    /**
     * Get order by ID (blocking gRPC call).
     * This method blocks until the gRPC response is received.
     * 
     * @param orderId The order ID
     * @return The order details
     */
    @UnaryRPCCall(service = "order-service", cbEnabled = true, circuitBreaker = "order-service-cb", bhEnabled = true, bulkhead = "order-service-bh", fqPackageName = "com.ravi9a2.example.proto", className = "OrderService", methodName = "GetOrder", method = RPCMethod.UNARY)
    User getOrder(@PathParam("orderId") String orderId);

    /**
     * Create a new order (blocking gRPC call).
     * 
     * @param order The order to create
     * @return The created order
     */
    @UnaryRPCCall(service = "order-service", cbEnabled = true, circuitBreaker = "order-service-cb", fqPackageName = "com.ravi9a2.example.proto", className = "OrderService", methodName = "CreateOrder", method = RPCMethod.UNARY)
    User createOrder(@Payload User order);
}
