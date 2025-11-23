package com.ravi9a2.example.service;

import com.ravi9a2.example.model.User;
import com.ravi9a2.instrumentation.annotation.Instrumented;
import com.ravi9a2.instrumentation.enums.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Example service demonstrating the use of @Instrumented annotation
 * and integration with generated external service clients.
 * 
 * @author raviprakash
 */
@Service
@Slf4j
public class ExampleService {

    @Autowired
    private UserService userService; // Reactive service (WebClient)

    @Autowired
    private ProductService productService; // Non-reactive service (HttpClient)

    @Autowired
    private OrderService orderService; // gRPC service

    @Autowired(required = false)
    private PaymentService paymentService; // Reactive service (can use Hystrix if configured)

    @Autowired(required = false)
    private HystrixExampleService hystrixExampleService; // Direct Hystrix usage examples

    /**
     * Example method using @Instrumented annotation for metrics collection.
     * This method will be automatically instrumented by InstrumentedAspect.
     */
    @Instrumented(metricType = MetricType.HTTP, tagSet = "operation=processUser,service=example-service")
    public Mono<User> processUser(String userId) {
        log.info("Processing user: {}", userId);

        // Simulate some business logic
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(50, 200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Call external service (this will also be instrumented via @Instrumented in
        // generated code)
        return userService.getUser(userId)
                .doOnNext(user -> log.info("Retrieved user: {}", user))
                .doOnError(error -> log.error("Error retrieving user: {}", error.getMessage()));
    }

    /**
     * Example method demonstrating manual metric collection.
     */
    @Instrumented(metricType = MetricType.DATABASE, tagSet = "operation=queryUsers,table=users")
    public List<User> queryUsers() {
        log.info("Querying users from database");

        // Simulate database query
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<User> users = new ArrayList<>();
        users.add(new User("1", "John Doe", "john@example.com", 30));
        users.add(new User("2", "Jane Smith", "jane@example.com", 25));

        return users;
    }

    /**
     * Example method that may throw exceptions (to demonstrate failure metrics).
     */
    @Instrumented(metricType = MetricType.HTTP, tagSet = "operation=riskyOperation,service=example-service")
    public String riskyOperation(boolean shouldFail) {
        if (shouldFail) {
            throw new RuntimeException("Simulated failure");
        }
        return "Success";
    }

    /**
     * Example method using non-reactive (blocking) HTTP service.
     * This demonstrates blocking calls using Apache HttpClient.
     */
    @Instrumented(metricType = MetricType.HTTP, tagSet = "operation=getProduct,service=example-service")
    public User getProduct(String productId) {
        log.info("Getting product: {} (blocking call)", productId);
        // This is a blocking call - will wait for response
        return productService.getProduct(productId);
    }

    /**
     * Example method using gRPC service.
     * This demonstrates gRPC unary calls.
     */
    @Instrumented(metricType = MetricType.RPC, tagSet = "operation=getOrder,service=example-service")
    public User getOrder(String orderId) {
        log.info("Getting order: {} (gRPC call)", orderId);
        // This is a blocking gRPC call
        return orderService.getOrder(orderId);
    }

    /**
     * Example method using payment service (can use Hystrix if configured).
     * This demonstrates reactive service with Hystrix protection.
     */
    @Instrumented(metricType = MetricType.HTTP, tagSet = "operation=getPayment,service=example-service")
    public Mono<User> getPayment(String paymentId) {
        if (paymentService == null) {
            log.warn("PaymentService not available");
            return Mono.error(new IllegalStateException("PaymentService not configured"));
        }
        log.info("Getting payment: {} (reactive call with Hystrix)", paymentId);
        return paymentService.getPayment(paymentId);
    }
}
