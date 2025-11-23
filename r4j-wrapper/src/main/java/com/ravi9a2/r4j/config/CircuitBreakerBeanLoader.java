package com.ravi9a2.r4j.config;

import com.ravi9a2.r4j.Metrics;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ravi9a2.r4j.Metrics.CB_FAILURE_RATE_METRIC_NAME;
import static com.ravi9a2.r4j.Metrics.CB_NOT_PERMITTED_METRIC_NAME;
import static com.ravi9a2.r4j.Metrics.CB_SLOW_CALL_RATE_METRIC_NAME;
import static com.ravi9a2.r4j.Metrics.CB_STATES_TRANSITIONS_METRIC_NAME;

/**
 * CircuitBreaker bean loader configuration class that
 * exposes the beans via CircuitBreakerRegistry.
 *
 * @author raviprakash
 */
@Configuration
public class CircuitBreakerBeanLoader {
    public static final String DEFAULT = "default";
    public static final String FAILURE_RATE_THRESHOLD = "failureRateThreshold";
    public static final String FAILURE_RATE_THRESHOLD_DEFAULT = "50";
    public static final String PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE = "permittedNumberOfCallsInHalfOpenState";
    public static final String PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE_DEFAULT = "10";
    public static final String SLIDING_WINDOW_SIZE = "slidingWindowSize";
    public static final String SLIDING_WINDOW_SIZE_DEFAULT = "100";
    public static final String SLIDING_WINDOW_TYPE = "slidingWindowType";
    public static final String SLIDING_WINDOW_TYPE_DEFAULT = "COUNT_BASED";
    public static final String MINIMUM_NUMBER_OF_CALLS = "minimumNumberOfCalls";
    public static final String MINIMUM_NUMBER_OF_CALLS_DEFAULT = "100";
    public static final String WRITABLE_STACK_TRACE_ENABLED = "writableStackTraceEnabled";
    public static final String WRITABLE_STACK_TRACE_ENABLED_DEFAULT = "false";
    public static final String AUTOMATIC_TRANSITION_FROM_OPEN_TO_HALF_OPEN_ENABLED = "automaticTransitionFromOpenToHalfOpenEnabled";
    public static final String AUTOMATIC_TRANSITION_FROM_OPEN_TO_HALF_OPEN_ENABLED_DEFAULT = "true";
    public static final String SLOW_CALL_RATE_THRESHOLD = "slowCallRateThreshold";
    public static final String SLOW_CALL_RATE_THRESHOLD_DEFAULT = "100";
    public static final String SLOW_CALL_DURATION_THRESHOLD = "slowCallDurationThreshold";
    public static final String SLOW_CALL_DURATION_THRESHOLD_DEFAULT = "60";
    public static final String MAX_WAIT_DURATION_IN_HALF_OPEN_STATE = "maxWaitDurationInHalfOpenState";
    public static final String MAX_WAIT_DURATION_IN_HALF_OPEN_STATE_DEFAULT = "1000";
    public static final String WAIT_DURATION_IN_OPEN_STATE = "waitDurationInOpenState";
    public static final String WAIT_DURATION_IN_OPEN_STATE_DEFAULT = "60000";
    public static final String CB_STATE_TRANSITION_MSG = "[CB StateTransition] Name: {} From: {} To: {}";
    public static final String CB_FAILURE_RATE_EXCEEDED_MSG = "[CB FailureRateExceeded] Name: {} Rate:{}";
    public static final String CB_SLOW_CALL_RATE_EXCEEDED_MSG = "[CB SlowCallRateExceeded] Name: {} Rate:{}";
    public static final String CB_CALL_NOT_PERMITTED_MSG = "[CB CallNotPermitted] Name: {}";
    public static final String CB_RESET_MSG = "[CB Reset] Name: {}";
    public static final String CB_ERROR_MSG = "[CB Error] Name: {} Message: {}";
    public static final String CB_SETUP_COMPLETE_MSG = "[CB Event] CircuitBreaker setup complete.";
    public static final String CB_REGISTRY_EVENT_MSG = "[CB Event] Registry event: {}";
    public static final String CB_REGISTRY_SETUP_COMPLETE_MSG = "[CB Event] CircuitBreakerRegistry setup complete.";
    public static final String RECORD_EXCEPTIONS = "recordExceptions";
    public static final String RECORD_EXCEPTION_DEFAULT = "com.ravi9a2.nca.exceptions.NetworkClientException";
    public static final String IGNORE_EXCEPTIONS = "ignoreExceptions";
    public static final String IGNORE_EXCEPTION_DEFAULT = "com.ravi9a2.nca.exceptions.Status4XXException";
    private static final Logger LOGGER = LogManager.getLogger(CircuitBreakerBeanLoader.class);

    private static Class<? extends Throwable> getClass(String name) {
        try {
            return (Class<? extends Throwable>) Class.forName(name);
        } catch (ClassNotFoundException | ClassCastException e) {
            return null;
        }
    }

    @Bean("allRawCBConfigs")
    @ConfigurationProperties(prefix = "r4j.circuit-breaker")
    public Map<String, Map<String, String>> allRawCBConfigs() {
        return new HashMap<>();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(
            @Qualifier("allRawCBConfigs") Map<String, Map<String, String>> allRawCBConfigs) {
        Map<String, CircuitBreakerConfig> allCBConfigs = allRawCBConfigs.entrySet().stream()
                .filter(e -> Objects.nonNull(e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> constructCircuitBreakerConfig(e.getValue(), allRawCBConfigs.get(DEFAULT))));
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(allCBConfigs);
        registerEventsOnRegistry(registry);
        registerEvents(allCBConfigs, registry);
        return registry;
    }

    private CircuitBreakerConfig constructCircuitBreakerConfig(Map<String, String> c, Map<String, String> d) {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(
                        Float.parseFloat(getValue(c, d, FAILURE_RATE_THRESHOLD, FAILURE_RATE_THRESHOLD_DEFAULT)))
                .permittedNumberOfCallsInHalfOpenState(
                        Integer.parseInt(getValue(c, d, PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE,
                                PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE_DEFAULT)))
                .slidingWindowSize(Integer.parseInt(getValue(c, d, SLIDING_WINDOW_SIZE, SLIDING_WINDOW_SIZE_DEFAULT)))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType
                        .valueOf(getValue(c, d, SLIDING_WINDOW_TYPE, SLIDING_WINDOW_TYPE_DEFAULT)))
                .minimumNumberOfCalls(
                        Integer.parseInt(getValue(c, d, MINIMUM_NUMBER_OF_CALLS, MINIMUM_NUMBER_OF_CALLS_DEFAULT)))
                .writableStackTraceEnabled(Boolean.parseBoolean(
                        (getValue(c, d, WRITABLE_STACK_TRACE_ENABLED, WRITABLE_STACK_TRACE_ENABLED_DEFAULT))))
                .automaticTransitionFromOpenToHalfOpenEnabled(
                        Boolean.parseBoolean((getValue(c, d, AUTOMATIC_TRANSITION_FROM_OPEN_TO_HALF_OPEN_ENABLED,
                                AUTOMATIC_TRANSITION_FROM_OPEN_TO_HALF_OPEN_ENABLED_DEFAULT))))
                .slowCallRateThreshold(
                        Float.parseFloat(getValue(c, d, SLOW_CALL_RATE_THRESHOLD, SLOW_CALL_RATE_THRESHOLD_DEFAULT)))
                .slowCallDurationThreshold(Duration.ofMillis(Long
                        .parseLong(getValue(c, d, SLOW_CALL_DURATION_THRESHOLD, SLOW_CALL_DURATION_THRESHOLD_DEFAULT))))
                .maxWaitDurationInHalfOpenState(Duration.ofMillis(Long.parseLong(getValue(c, d,
                        MAX_WAIT_DURATION_IN_HALF_OPEN_STATE, MAX_WAIT_DURATION_IN_HALF_OPEN_STATE_DEFAULT))))
                .waitDurationInOpenState(Duration.ofMillis(Long
                        .parseLong(getValue(c, d, WAIT_DURATION_IN_OPEN_STATE, WAIT_DURATION_IN_OPEN_STATE_DEFAULT))))
                .recordExceptions(exceptions(c, d, RECORD_EXCEPTIONS, RECORD_EXCEPTION_DEFAULT))
                .ignoreExceptions(exceptions(c, d, IGNORE_EXCEPTIONS, IGNORE_EXCEPTION_DEFAULT))
                .build();
    }

    private void registerEvents(Map<String, CircuitBreakerConfig> allCBConfigs, CircuitBreakerRegistry registry) {
        allCBConfigs.keySet().forEach(key -> {
            CircuitBreaker cb = registry.circuitBreaker(key, key);
            LOGGER.info(CB_SETUP_COMPLETE_MSG);
            cb.getEventPublisher()
                    .onStateTransition(e -> {
                        Metrics.increment(CB_STATES_TRANSITIONS_METRIC_NAME,
                                "cbName=" + e.getCircuitBreakerName() + ",fromState="
                                        + e.getStateTransition().getFromState()
                                        + ",toState=" + e.getStateTransition().getToState());
                        LOGGER.info(CB_STATE_TRANSITION_MSG, e.getCircuitBreakerName(),
                                e.getStateTransition().getFromState(), e.getStateTransition().getToState());
                    })
                    .onFailureRateExceeded(e -> {
                        Metrics.increment(CB_FAILURE_RATE_METRIC_NAME,
                                "cbName=" + e.getCircuitBreakerName() + ",failureRate=" + e.getFailureRate());
                        LOGGER.error(CB_FAILURE_RATE_EXCEEDED_MSG, e.getCircuitBreakerName(), e.getFailureRate());
                    })
                    .onSlowCallRateExceeded(e -> {
                        Metrics.increment(CB_SLOW_CALL_RATE_METRIC_NAME,
                                "cbName=" + e.getCircuitBreakerName() + ",slowCallRate=" + e.getSlowCallRate());
                        LOGGER.error(CB_SLOW_CALL_RATE_EXCEEDED_MSG, e.getCircuitBreakerName(), e.getSlowCallRate());
                    })
                    .onCallNotPermitted(e -> {
                        Metrics.increment(CB_NOT_PERMITTED_METRIC_NAME, "cbName=" + e.getCircuitBreakerName());
                        LOGGER.error(CB_CALL_NOT_PERMITTED_MSG, e.getCircuitBreakerName());
                    })
                    .onReset(event -> LOGGER.info(CB_RESET_MSG, event.getCircuitBreakerName()))
                    .onError(event -> LOGGER.error(CB_ERROR_MSG, event.getCircuitBreakerName(),
                            event.getThrowable().getMessage()));
        });
    }

    private void registerEventsOnRegistry(CircuitBreakerRegistry registry) {
        registry.getEventPublisher().onEvent(registryEvent -> {
            LOGGER.info(CB_REGISTRY_EVENT_MSG, registryEvent.getEventType().name());
        });
        LOGGER.info(CB_REGISTRY_SETUP_COMPLETE_MSG);
    }

    private String getValue(Map<String, String> c, Map<String, String> d, String k, String v) {
        if (Objects.isNull(d))
            d = Collections.emptyMap();
        if (Objects.isNull(c))
            c = Collections.emptyMap();
        return c.getOrDefault(k, d.getOrDefault(k, v));
    }

    private Class<? extends Throwable>[] exceptions(Map<String, String> c, Map<String, String> d, String k, String v) {
        String[] classNames = getValue(c, d, k, v).split(",");
        Class<? extends Throwable>[] classes = new Class[classNames.length];
        int i = 0;
        for (String className : classNames) {
            Class<? extends Throwable> clazz = getClass(className);
            if (Objects.nonNull(clazz)) {
                classes[i++] = clazz;
            }
        }
        return classes;
    }

}
