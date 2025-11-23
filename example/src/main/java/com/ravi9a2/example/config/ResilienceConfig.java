package com.ravi9a2.example.config;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for Resilience4j components.
 * This provides circuit breaker and bulkhead registries for the framework.
 * 
 * @author raviprakash
 */
@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(10000))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .build();

        return CircuitBreakerRegistry.of(defaultConfig);
    }

    @Bean
    public BulkheadRegistry semaphoreBulkheadRegistry() {
        BulkheadConfig defaultConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(10)
                .build();

        return BulkheadRegistry.of(defaultConfig);
    }

    @Bean
    public io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry() {
        io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig defaultConfig = 
                io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(10)
                .coreThreadPoolSize(2)
                .queueCapacity(100)
                .build();

        return io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry.of(defaultConfig);
    }
}

