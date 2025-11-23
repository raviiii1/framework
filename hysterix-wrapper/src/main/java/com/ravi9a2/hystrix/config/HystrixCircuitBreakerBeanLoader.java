package com.ravi9a2.hystrix.config;

import com.netflix.hystrix.HystrixCommandProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Circuit Breaker bean loader configuration class that
 * loads Hystrix command properties from configuration and
 * exposes them via HystrixCommandPropertiesRegistry.
 *
 * @author raviprakash
 */
@Configuration
public class HystrixCircuitBreakerBeanLoader {

    public static final String DEFAULT = "default";
    public static final String CIRCUIT_BREAKER_ENABLED = "circuitBreakerEnabled";
    public static final String CIRCUIT_BREAKER_ENABLED_DEFAULT = "true";
    public static final String CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD = "circuitBreakerRequestVolumeThreshold";
    public static final String CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD_DEFAULT = "20";
    public static final String CIRCUIT_BREAKER_SLEEP_WINDOW = "circuitBreakerSleepWindowInMilliseconds";
    public static final String CIRCUIT_BREAKER_SLEEP_WINDOW_DEFAULT = "5000";
    public static final String CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE = "circuitBreakerErrorThresholdPercentage";
    public static final String CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE_DEFAULT = "50";
    public static final String EXECUTION_TIMEOUT = "executionTimeoutInMilliseconds";
    public static final String EXECUTION_TIMEOUT_DEFAULT = "10000";
    public static final String EXECUTION_ISOLATION_THREAD_TIMEOUT = "executionIsolationThreadTimeoutInMilliseconds";
    public static final String EXECUTION_ISOLATION_THREAD_TIMEOUT_DEFAULT = "10000";
    public static final String EXECUTION_ISOLATION_STRATEGY = "executionIsolationStrategy";
    public static final String EXECUTION_ISOLATION_STRATEGY_DEFAULT = "THREAD";

    private static final Logger LOGGER = LoggerFactory.getLogger(HystrixCircuitBreakerBeanLoader.class);
    private static final String CB_SETUP_COMPLETE_MSG = "[Hystrix CB] Circuit breaker setup complete for: {}";
    private static final String CB_REGISTRY_SETUP_COMPLETE_MSG = "[Hystrix CB] CircuitBreakerRegistry setup complete.";

    @Bean("allRawHystrixCBConfigs")
    @ConfigurationProperties(prefix = "hystrix.circuit-breaker")
    public Map<String, Map<String, String>> allRawHystrixCBConfigs() {
        return new HashMap<>();
    }

    @Bean
    public HystrixCommandPropertiesRegistry hystrixCommandPropertiesRegistry(
            @Qualifier("allRawHystrixCBConfigs") Map<String, Map<String, String>> allRawHystrixCBConfigs) {

        HystrixCommandPropertiesRegistry registry = new HystrixCommandPropertiesRegistry();

        Map<String, HystrixCommandProperties.Setter> allCBConfigs = allRawHystrixCBConfigs.entrySet().stream()
                .filter(e -> Objects.nonNull(e.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> constructHystrixCommandProperties(e.getValue(), allRawHystrixCBConfigs.get(DEFAULT))
                ));

        allCBConfigs.forEach((name, properties) -> {
            registry.register(name, properties);
            LOGGER.info(CB_SETUP_COMPLETE_MSG, name);
        });

        LOGGER.info(CB_REGISTRY_SETUP_COMPLETE_MSG);
        return registry;
    }

    private HystrixCommandProperties.Setter constructHystrixCommandProperties(
            Map<String, String> config, Map<String, String> defaultConfig) {

        HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter();

        setter.withCircuitBreakerEnabled(
                Boolean.parseBoolean(getValue(config, defaultConfig, CIRCUIT_BREAKER_ENABLED, CIRCUIT_BREAKER_ENABLED_DEFAULT))
        );

        setter.withCircuitBreakerRequestVolumeThreshold(
                Integer.parseInt(getValue(config, defaultConfig, CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD_DEFAULT))
        );

        setter.withCircuitBreakerSleepWindowInMilliseconds(
                Integer.parseInt(getValue(config, defaultConfig, CIRCUIT_BREAKER_SLEEP_WINDOW, CIRCUIT_BREAKER_SLEEP_WINDOW_DEFAULT))
        );

        setter.withCircuitBreakerErrorThresholdPercentage(
                Integer.parseInt(getValue(config, defaultConfig, CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE, CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE_DEFAULT))
        );

        setter.withExecutionTimeoutInMilliseconds(
                Integer.parseInt(getValue(config, defaultConfig, EXECUTION_TIMEOUT, EXECUTION_TIMEOUT_DEFAULT))
        );

        setter.withExecutionIsolationThreadTimeoutInMilliseconds(
                Integer.parseInt(getValue(config, defaultConfig, EXECUTION_ISOLATION_THREAD_TIMEOUT, EXECUTION_ISOLATION_THREAD_TIMEOUT_DEFAULT))
        );

        String isolationStrategy = getValue(config, defaultConfig, EXECUTION_ISOLATION_STRATEGY, EXECUTION_ISOLATION_STRATEGY_DEFAULT);
        if ("SEMAPHORE".equalsIgnoreCase(isolationStrategy)) {
            setter.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE);
        } else {
            setter.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD);
        }

        return setter;
    }

    private String getValue(Map<String, String> config, Map<String, String> defaultConfig, String key, String defaultValue) {
        if (Objects.isNull(defaultConfig)) {
            defaultConfig = Collections.emptyMap();
        }
        if (Objects.isNull(config)) {
            config = Collections.emptyMap();
        }
        return config.getOrDefault(key, defaultConfig.getOrDefault(key, defaultValue));
    }
}

