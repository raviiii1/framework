package com.ravi9a2.example.controller;

import com.ravi9a2.example.model.User;
import com.ravi9a2.example.service.ExampleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Example REST controller demonstrating the framework usage.
 * 
 * @author raviprakash
 */
@RestController
@RequestMapping("/api/example")
@Slf4j
public class ExampleController {

    @Autowired
    private ExampleService exampleService;

    /**
     * Example endpoint that uses the instrumented service.
     */
    @GetMapping("/users/{userId}")
    public Mono<User> getUser(@PathVariable String userId) {
        log.info("Received request for user: {}", userId);
        return exampleService.processUser(userId);
    }

    /**
     * Example endpoint demonstrating database metrics.
     */
    @GetMapping("/users")
    public List<User> getAllUsers() {
        log.info("Received request for all users");
        return exampleService.queryUsers();
    }

    /**
     * Example endpoint that may fail (to demonstrate failure metrics).
     */
    @GetMapping("/risky")
    public String riskyOperation(@RequestParam(defaultValue = "false") boolean fail) {
        log.info("Received risky operation request, fail={}", fail);
        return exampleService.riskyOperation(fail);
    }

    /**
     * Example endpoint using non-reactive (blocking) HTTP service.
     * Demonstrates blocking calls with Apache HttpClient.
     */
    @GetMapping("/products/{productId}")
    public User getProduct(@PathVariable String productId) {
        log.info("Received request for product: {} (blocking call)", productId);
        return exampleService.getProduct(productId);
    }

    /**
     * Example endpoint using gRPC service.
     * Demonstrates gRPC unary calls.
     */
    @GetMapping("/orders/{orderId}")
    public User getOrder(@PathVariable String orderId) {
        log.info("Received request for order: {} (gRPC call)", orderId);
        return exampleService.getOrder(orderId);
    }

    /**
     * Example endpoint using payment service (with Hystrix protection).
     * Demonstrates reactive service with Hystrix circuit breaker.
     */
    @GetMapping("/payments/{paymentId}")
    public Mono<User> getPayment(@PathVariable String paymentId) {
        log.info("Received request for payment: {} (Hystrix protected)", paymentId);
        return exampleService.getPayment(paymentId);
    }

    @Autowired(required = false)
    private com.ravi9a2.example.service.HystrixExampleService hystrixExampleService;

    /**
     * Example endpoint using Hystrix reactive executor directly.
     * Demonstrates direct Hystrix usage.
     */
    @GetMapping("/hystrix/payments/{paymentId}")
    public Mono<User> getPaymentWithHystrix(@PathVariable String paymentId) {
        log.info("Received request for payment via Hystrix: {}", paymentId);
        if (hystrixExampleService == null) {
            return Mono.error(new IllegalStateException("HystrixExampleService not available"));
        }
        return hystrixExampleService.getPaymentWithHystrix(paymentId);
    }

    /**
     * Example endpoint using Hystrix non-reactive executor (blocking).
     * Demonstrates blocking calls with Hystrix.
     */
    @GetMapping("/hystrix/payments-blocking/{paymentId}")
    public User getPaymentBlockingWithHystrix(@PathVariable String paymentId) {
        log.info("Received request for payment via Hystrix (blocking): {}", paymentId);
        if (hystrixExampleService == null) {
            throw new IllegalStateException("HystrixExampleService not available");
        }
        return hystrixExampleService.getPaymentBlockingWithHystrix(paymentId);
    }

    /**
     * Example endpoint to test Hystrix circuit breaker behavior.
     */
    @GetMapping("/hystrix/test-circuit-breaker")
    public Mono<String> testHystrixCircuitBreaker(@RequestParam(defaultValue = "false") boolean fail) {
        log.info("Testing Hystrix circuit breaker, fail={}", fail);
        if (hystrixExampleService == null) {
            return Mono.just("HystrixExampleService not available");
        }
        return hystrixExampleService.testHystrixCircuitBreaker(fail);
    }
}
