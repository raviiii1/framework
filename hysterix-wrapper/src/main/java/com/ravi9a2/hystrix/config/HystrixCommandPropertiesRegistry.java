package com.ravi9a2.hystrix.config;

import com.netflix.hystrix.HystrixCommandProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Hystrix command properties configurations.
 * Similar to CircuitBreakerRegistry in R4J.
 *
 * @author raviprakash
 */
public class HystrixCommandPropertiesRegistry {

    private final Map<String, HystrixCommandProperties.Setter> commandPropertiesMap;

    public HystrixCommandPropertiesRegistry() {
        this.commandPropertiesMap = new ConcurrentHashMap<>();
    }

    public void register(String name, HystrixCommandProperties.Setter properties) {
        commandPropertiesMap.put(name, properties);
    }

    public HystrixCommandProperties.Setter getCommandProperties(String name) {
        return commandPropertiesMap.getOrDefault(name, getDefaultCommandProperties());
    }

    public HystrixCommandProperties.Setter getDefaultCommandProperties() {
        return HystrixCommandProperties.Setter()
                .withCircuitBreakerEnabled(true)
                .withCircuitBreakerRequestVolumeThreshold(20)
                .withCircuitBreakerSleepWindowInMilliseconds(5000)
                .withCircuitBreakerErrorThresholdPercentage(50)
                .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                .withExecutionTimeoutInMilliseconds(10000)
                .withExecutionIsolationThreadTimeoutInMilliseconds(10000);
    }
}
