package com.ravi9a2.example.service;

import com.ravi9a2.example.model.User;
import com.ravi9a2.hystrix.HystrixNonReactiveExecutor;
import com.ravi9a2.hystrix.HystrixReactiveExecutor;
import com.ravi9a2.instrumentation.annotation.Instrumented;
import com.ravi9a2.instrumentation.enums.MetricType;
import com.ravi9a2.nea.core.data.HTTPMethod;
import com.ravi9a2.nea.core.data.RestCallDefinition;
import com.ravi9a2.nea.core.data.Type;
import com.ravi9a2.nca.NonReactiveClient;
import com.ravi9a2.nca.ReactiveClient;
import com.ravi9a2.nca.data.RestRequestSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Example service demonstrating direct usage of Hystrix executors.
 * 
 * This service shows how to use Hystrix executors directly without code
 * generation,
 * giving you full control over the call definition and Hystrix configuration.
 * 
 * @author raviprakash
 */
@Service
@Slf4j
public class HystrixExampleService {

    @Autowired(required = false)
    private HystrixReactiveExecutor<WebClient> hystrixReactiveExecutor;

    @Autowired(required = false)
    @Qualifier("reactiveWebClientRegistry")
    private com.ravi9a2.nca.ReactiveClientRegistry reactiveClientRegistry;

    @Autowired(required = false)
    private HystrixNonReactiveExecutor<org.apache.http.client.HttpClient> hystrixNonReactiveExecutor;

    @Autowired(required = false)
    @Qualifier("clientConfigRegistry")
    private com.ravi9a2.nca.NonReactiveClientRegistry nonReactiveClientRegistry;

    /**
     * Example method using Hystrix reactive executor directly.
     * This demonstrates how to use Hystrix with reactive calls.
     */
    @Instrumented(metricType = MetricType.HTTP, tagSet = "operation=getPaymentWithHystrix,service=example-service,executor=hystrix")
    public Mono<User> getPaymentWithHystrix(String paymentId) {
        if (hystrixReactiveExecutor == null || reactiveClientRegistry == null) {
            log.warn("Hystrix executors not available. Make sure hystrix-wrapper is configured.");
            return Mono.error(new IllegalStateException("Hystrix executors not configured"));
        }

        log.info("Getting payment: {} using Hystrix reactive executor", paymentId);

        // Get the reactive client
        ReactiveClient<WebClient> client = reactiveClientRegistry.client("payment-service");

        // Build the call definition
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("paymentId", paymentId);

        RestCallDefinition callDefinition = RestCallDefinition.builder()
                .isCircuitBreakerEnabled(true)
                .isBulkheadEnabled(true)
                .serviceTag("payment-service")
                .isSilentFailure(false)
                .isRetryable(false)
                .path("/api/payments/{paymentId}")
                .responseType(User.class)
                .payload(null)
                .cbTag("payment-service-cb")
                .bhTag("payment-service-bh")
                .retryTag("payment-service")
                .httpHeaders(new HashMap<>())
                .pathParams(pathParams)
                .type(Type.HTTP)
                .httpMethod(HTTPMethod.GET)
                .build();

        // Execute using Hystrix reactive executor
        return hystrixReactiveExecutor.executeToMono(client, callDefinition)
                .doOnNext(user -> log.info("Retrieved payment via Hystrix: {}", user))
                .doOnError(error -> log.error("Error retrieving payment via Hystrix: {}", error.getMessage()));
    }

    /**
     * Example method using Hystrix non-reactive executor directly.
     * This demonstrates how to use Hystrix with blocking calls.
     */
    @Instrumented(metricType = MetricType.HTTP, tagSet = "operation=getPaymentBlockingWithHystrix,service=example-service,executor=hystrix")
    public User getPaymentBlockingWithHystrix(String paymentId) {
        if (hystrixNonReactiveExecutor == null || nonReactiveClientRegistry == null) {
            log.warn("Hystrix non-reactive executors not available. Make sure hystrix-wrapper is configured.");
            throw new IllegalStateException("Hystrix executors not configured");
        }

        log.info("Getting payment: {} using Hystrix non-reactive executor (blocking)", paymentId);

        // Get the non-reactive client
        NonReactiveClient<org.apache.http.client.HttpClient> client = nonReactiveClientRegistry
                .client("payment-service");

        // Build the call definition
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("paymentId", paymentId);

        RestCallDefinition callDefinition = RestCallDefinition.builder()
                .isCircuitBreakerEnabled(true)
                .isBulkheadEnabled(true)
                .serviceTag("payment-service")
                .isSilentFailure(false)
                .isRetryable(false)
                .path("/api/payments/{paymentId}")
                .responseType(User.class)
                .payload(null)
                .cbTag("payment-service-cb")
                .bhTag("payment-service-bh")
                .retryTag("payment-service")
                .httpHeaders(new HashMap<>())
                .pathParams(pathParams)
                .type(Type.HTTP)
                .httpMethod(HTTPMethod.GET)
                .build();

        // Execute using Hystrix non-reactive executor (blocking)
        return hystrixNonReactiveExecutor.execute(client, callDefinition);
    }

    /**
     * Example method demonstrating Hystrix circuit breaker behavior.
     * This will trigger circuit breaker if failures exceed threshold.
     */
    @Instrumented(metricType = MetricType.HTTP, tagSet = "operation=testHystrixCircuitBreaker,service=example-service,executor=hystrix")
    public Mono<String> testHystrixCircuitBreaker(boolean shouldFail) {
        if (hystrixReactiveExecutor == null || reactiveClientRegistry == null) {
            return Mono.just("Hystrix not configured");
        }

        log.info("Testing Hystrix circuit breaker, shouldFail={}", shouldFail);

        ReactiveClient<WebClient> client = reactiveClientRegistry.client("payment-service");

        // Use a path that will fail if shouldFail is true
        String path = shouldFail ? "/api/payments/invalid-endpoint" : "/api/payments/1";

        RestCallDefinition callDefinition = RestCallDefinition.builder()
                .isCircuitBreakerEnabled(true)
                .isBulkheadEnabled(false)
                .serviceTag("payment-service")
                .isSilentFailure(true)
                .isRetryable(false)
                .path(path)
                .responseType(User.class)
                .payload(null)
                .cbTag("payment-service-cb")
                .bhTag("payment-service-bh")
                .retryTag("payment-service")
                .httpHeaders(new HashMap<>())
                .pathParams(new HashMap<>())
                .type(Type.HTTP)
                .httpMethod(HTTPMethod.GET)
                .build();

        return hystrixReactiveExecutor.executeToMono(client, callDefinition)
                .map(user -> "Success: " + user)
                .onErrorReturn("Circuit breaker may have opened or call failed")
                .doOnNext(result -> log.info("Hystrix test result: {}", result));
    }
}
