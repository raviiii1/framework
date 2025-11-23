package com.ravi9a2.example.service;

import com.ravi9a2.example.model.User;
import com.ravi9a2.nea.annotations.ExternalService;
import com.ravi9a2.nea.annotations.GetCall;
import com.ravi9a2.nea.annotations.PathParam;
import com.ravi9a2.nea.annotations.PostCall;
import com.ravi9a2.nea.annotations.Payload;
import reactor.core.publisher.Mono;

/**
 * Payment service interface demonstrating Hystrix usage.
 * 
 * This service will use Hystrix executors when Hystrix wrapper is configured.
 * The annotation processor will generate PaymentServiceImpl with Hystrix
 * protection
 * if Hystrix executors are available in the Spring context.
 * 
 * Note: The generated code will use whichever executor (R4J or Hystrix) is
 * available.
 * To specifically use Hystrix, ensure Hystrix executors are the only ones
 * configured,
 * or use the direct Hystrix service example.
 */
@ExternalService
public interface PaymentService {

    /**
     * Get payment by ID (reactive call with Hystrix protection).
     */
    @GetCall(path = "/api/payments/{paymentId}", service = "payment-service", cbEnabled = true, circuitBreaker = "payment-service-cb", bhEnabled = true, bulkhead = "payment-service-bh")
    Mono<User> getPayment(@PathParam("paymentId") String paymentId);

    /**
     * Process a payment (reactive call with Hystrix protection).
     */
    @PostCall(path = "/api/payments", service = "payment-service", cbEnabled = true, circuitBreaker = "payment-service-cb")
    Mono<User> processPayment(@Payload User payment);
}
