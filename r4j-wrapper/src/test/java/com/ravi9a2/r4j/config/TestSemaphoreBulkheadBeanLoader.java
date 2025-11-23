package com.ravi9a2.r4j.config;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static com.ravi9a2.r4j.config.SemaphoreBulkheadBeanLoader.FAIR_CALL_HANDLING_ENABLED;
import static com.ravi9a2.r4j.config.SemaphoreBulkheadBeanLoader.FAIR_CALL_HANDLING_ENABLED_DEFAULT;
import static com.ravi9a2.r4j.config.SemaphoreBulkheadBeanLoader.MAX_CONCURRENT_CALLS;
import static com.ravi9a2.r4j.config.SemaphoreBulkheadBeanLoader.MAX_CONCURRENT_CALLS_DEFAULT;
import static com.ravi9a2.r4j.config.SemaphoreBulkheadBeanLoader.MAX_WAIT_DURATION;
import static com.ravi9a2.r4j.config.SemaphoreBulkheadBeanLoader.MAX_WAIT_DURATION_DEFAULT;
import static com.ravi9a2.r4j.config.SemaphoreBulkheadBeanLoader.WRITABLE_STACK_TRACE_ENABLED;
import static com.ravi9a2.r4j.config.SemaphoreBulkheadBeanLoader.WRITABLE_STACK_TRACE_ENABLED_DEFAULT;

public class TestSemaphoreBulkheadBeanLoader {


    @InjectMocks
    SemaphoreBulkheadBeanLoader semaphoreBulkheadBeanLoader;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_defaultConfigs() {
        HashMap<String, Map<String, String>> smConfigs = new HashMap<>();
        HashMap<String, String> smConfig1 = new HashMap<>();
        smConfigs.put("semaphoreConfig1", smConfig1);

        BulkheadRegistry bulkheadRegistry = semaphoreBulkheadBeanLoader.semaphoreBulkheadRegistry(smConfigs);

        BulkheadConfig semaphoreConfig1 = bulkheadRegistry.getConfiguration("semaphoreConfig1").get();

        Assertions.assertEquals(Integer.valueOf(MAX_CONCURRENT_CALLS_DEFAULT), semaphoreConfig1.getMaxConcurrentCalls());
        Assertions.assertEquals(Long.valueOf(MAX_WAIT_DURATION_DEFAULT), semaphoreConfig1.getMaxWaitDuration().toMillis());
        Assertions.assertEquals(Boolean.parseBoolean(WRITABLE_STACK_TRACE_ENABLED_DEFAULT), semaphoreConfig1.isWritableStackTraceEnabled());
        Assertions.assertEquals(Boolean.parseBoolean(FAIR_CALL_HANDLING_ENABLED_DEFAULT), semaphoreConfig1.isFairCallHandlingEnabled());
    }

    @Test
    public void test_providedConfigs() {
        HashMap<String, Map<String, String>> smConfigs = new HashMap<>();
        HashMap<String, String> smConfig1 = new HashMap<>();

        smConfig1.put(MAX_CONCURRENT_CALLS, "50");
        smConfig1.put(MAX_WAIT_DURATION, "99");
        smConfig1.put(WRITABLE_STACK_TRACE_ENABLED, "true");
        smConfig1.put(FAIR_CALL_HANDLING_ENABLED, "true");
        smConfigs.put("semaphoreConfig1", smConfig1);

        BulkheadRegistry bulkheadRegistry = semaphoreBulkheadBeanLoader.semaphoreBulkheadRegistry(smConfigs);

        BulkheadConfig semaphoreConfig1 = bulkheadRegistry.getConfiguration("semaphoreConfig1").get();

        Assertions.assertEquals(Integer.valueOf("50"), semaphoreConfig1.getMaxConcurrentCalls());
        Assertions.assertEquals(Long.valueOf("99"), semaphoreConfig1.getMaxWaitDuration().toMillis());
        Assertions.assertEquals(Boolean.parseBoolean("true"), semaphoreConfig1.isWritableStackTraceEnabled());
        Assertions.assertEquals(Boolean.parseBoolean("true"), semaphoreConfig1.isFairCallHandlingEnabled());

    }

}
