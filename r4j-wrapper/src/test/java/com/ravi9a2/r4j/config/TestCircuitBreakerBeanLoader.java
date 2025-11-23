package com.ravi9a2.r4j.config;

import com.ravi9a2.nca.exceptions.NetworkClientException;
import com.ravi9a2.nca.exceptions.Status4XXException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.AUTOMATIC_TRANSITION_FROM_OPEN_TO_HALF_OPEN_ENABLED;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.AUTOMATIC_TRANSITION_FROM_OPEN_TO_HALF_OPEN_ENABLED_DEFAULT;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.FAILURE_RATE_THRESHOLD;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.FAILURE_RATE_THRESHOLD_DEFAULT;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.MAX_WAIT_DURATION_IN_HALF_OPEN_STATE;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.MAX_WAIT_DURATION_IN_HALF_OPEN_STATE_DEFAULT;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.MINIMUM_NUMBER_OF_CALLS;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.MINIMUM_NUMBER_OF_CALLS_DEFAULT;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE_DEFAULT;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.SLIDING_WINDOW_SIZE;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.SLIDING_WINDOW_SIZE_DEFAULT;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.SLIDING_WINDOW_TYPE;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.SLIDING_WINDOW_TYPE_DEFAULT;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.SLOW_CALL_DURATION_THRESHOLD;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.SLOW_CALL_DURATION_THRESHOLD_DEFAULT;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.SLOW_CALL_RATE_THRESHOLD;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.SLOW_CALL_RATE_THRESHOLD_DEFAULT;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.WAIT_DURATION_IN_OPEN_STATE;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.WAIT_DURATION_IN_OPEN_STATE_DEFAULT;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.WRITABLE_STACK_TRACE_ENABLED;
import static com.ravi9a2.r4j.config.CircuitBreakerBeanLoader.WRITABLE_STACK_TRACE_ENABLED_DEFAULT;

public class TestCircuitBreakerBeanLoader {


    @InjectMocks
    CircuitBreakerBeanLoader circuitBreakerBeanLoader;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_defaultConfigs() {
        HashMap<String, Map<String, String>> cbConfigs = new HashMap<>();
        HashMap<String, String> cbConfig1 = new HashMap<>();
        cbConfigs.put("cbConfig1", cbConfig1);
        CircuitBreakerRegistry circuitBreakerRegistry = circuitBreakerBeanLoader.circuitBreakerRegistry(cbConfigs);
        CircuitBreakerConfig circuitBreaker = circuitBreakerRegistry.getConfiguration("cbConfig1").get();
        Assertions.assertEquals(Float.valueOf(FAILURE_RATE_THRESHOLD_DEFAULT), circuitBreaker.getFailureRateThreshold());
        Assertions.assertEquals(Integer.valueOf(PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE_DEFAULT), circuitBreaker.getPermittedNumberOfCallsInHalfOpenState());
        Assertions.assertEquals(Integer.valueOf(SLIDING_WINDOW_SIZE_DEFAULT), circuitBreaker.getSlidingWindowSize());
        Assertions.assertEquals(SLIDING_WINDOW_TYPE_DEFAULT, circuitBreaker.getSlidingWindowType().toString());
        Assertions.assertEquals(Integer.valueOf(MINIMUM_NUMBER_OF_CALLS_DEFAULT), circuitBreaker.getMinimumNumberOfCalls());
        Assertions.assertEquals(Boolean.parseBoolean(WRITABLE_STACK_TRACE_ENABLED_DEFAULT), circuitBreaker.isWritableStackTraceEnabled());
        Assertions.assertEquals(Boolean.parseBoolean(AUTOMATIC_TRANSITION_FROM_OPEN_TO_HALF_OPEN_ENABLED_DEFAULT), circuitBreaker.isAutomaticTransitionFromOpenToHalfOpenEnabled());
        Assertions.assertEquals(Float.valueOf(SLOW_CALL_RATE_THRESHOLD_DEFAULT), circuitBreaker.getSlowCallRateThreshold());
        Assertions.assertEquals(Long.valueOf(SLOW_CALL_DURATION_THRESHOLD_DEFAULT), circuitBreaker.getSlowCallDurationThreshold().toMillis());
        Assertions.assertEquals(Long.valueOf(MAX_WAIT_DURATION_IN_HALF_OPEN_STATE_DEFAULT), circuitBreaker.getMaxWaitDurationInHalfOpenState().toMillis());
        Assertions.assertEquals(Long.valueOf(WAIT_DURATION_IN_OPEN_STATE_DEFAULT), circuitBreaker.getWaitDurationInOpenState().toMillis());
        Assertions.assertTrue(circuitBreaker.getRecordExceptionPredicate().test(new NetworkClientException("test", 400)));
        Assertions.assertFalse(circuitBreaker.getRecordExceptionPredicate().test(new Exception("test")));
        Assertions.assertTrue(circuitBreaker.getIgnoreExceptionPredicate().test(new Status4XXException("test", 400)));
        Assertions.assertFalse(circuitBreaker.getIgnoreExceptionPredicate().test(new Exception("test")));
    }

    @Test
    public void test_providedConfigs() {
        HashMap<String, Map<String, String>> cbConfigs = new HashMap<>();
        HashMap<String, String> cbConfig1 = new HashMap<>();
        cbConfigs.put("cbConfig1", cbConfig1);
        cbConfig1.put(FAILURE_RATE_THRESHOLD, "50");
        cbConfig1.put(PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE, "99");
        cbConfig1.put(SLIDING_WINDOW_SIZE, "200");
        cbConfig1.put(SLIDING_WINDOW_TYPE, "TIME_BASED");
        cbConfig1.put(MINIMUM_NUMBER_OF_CALLS, "33");
        cbConfig1.put(WRITABLE_STACK_TRACE_ENABLED, "true");
        cbConfig1.put(AUTOMATIC_TRANSITION_FROM_OPEN_TO_HALF_OPEN_ENABLED, "false");
        cbConfig1.put(SLOW_CALL_RATE_THRESHOLD, "50");
        cbConfig1.put(SLOW_CALL_DURATION_THRESHOLD, "666");
        cbConfig1.put(MAX_WAIT_DURATION_IN_HALF_OPEN_STATE, "777");
        cbConfig1.put(WAIT_DURATION_IN_OPEN_STATE, "888");

        MockitoAnnotations.initMocks(this);
        CircuitBreakerRegistry circuitBreakerRegistry = circuitBreakerBeanLoader.circuitBreakerRegistry(cbConfigs);
        CircuitBreakerConfig circuitBreaker = circuitBreakerRegistry.getConfiguration("cbConfig1").get();
        Assertions.assertEquals(Float.valueOf("50"), circuitBreaker.getFailureRateThreshold());
        Assertions.assertEquals(Integer.valueOf("99"), circuitBreaker.getPermittedNumberOfCallsInHalfOpenState());
        Assertions.assertEquals(Integer.valueOf("200"), circuitBreaker.getSlidingWindowSize());
        Assertions.assertEquals("TIME_BASED", circuitBreaker.getSlidingWindowType().toString());
        Assertions.assertEquals(Integer.valueOf("33"), circuitBreaker.getMinimumNumberOfCalls());
        Assertions.assertEquals(Boolean.parseBoolean("true"), circuitBreaker.isWritableStackTraceEnabled());
        Assertions.assertEquals(Boolean.parseBoolean("false"), circuitBreaker.isAutomaticTransitionFromOpenToHalfOpenEnabled());
        Assertions.assertEquals(Float.valueOf("50"), circuitBreaker.getSlowCallRateThreshold());
        Assertions.assertEquals(Long.valueOf("666"), circuitBreaker.getSlowCallDurationThreshold().toMillis());
        Assertions.assertEquals(Long.valueOf("777"), circuitBreaker.getMaxWaitDurationInHalfOpenState().toMillis());
        Assertions.assertEquals(Long.valueOf("888"), circuitBreaker.getWaitDurationInOpenState().toMillis());
    }

}
