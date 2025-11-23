package com.ravi9a2.r4j.config;

import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static com.ravi9a2.r4j.config.ThreadPoolBulkheadBeanLoader.CORE_THREAD_POOL_SIZE;
import static com.ravi9a2.r4j.config.ThreadPoolBulkheadBeanLoader.CORE_THREAD_POOL_SIZE_DEFAULT;
import static com.ravi9a2.r4j.config.ThreadPoolBulkheadBeanLoader.KEEP_ALIVE_DURATION;
import static com.ravi9a2.r4j.config.ThreadPoolBulkheadBeanLoader.KEEP_ALIVE_DURATION_DEFAULT;
import static com.ravi9a2.r4j.config.ThreadPoolBulkheadBeanLoader.MAX_THREAD_POOL_SIZE;
import static com.ravi9a2.r4j.config.ThreadPoolBulkheadBeanLoader.MAX_THREAD_POOL_SIZE_DEFAULT;
import static com.ravi9a2.r4j.config.ThreadPoolBulkheadBeanLoader.QUEUE_CAPACITY;
import static com.ravi9a2.r4j.config.ThreadPoolBulkheadBeanLoader.QUEUE_CAPACITY_DEFAULT;

public class TestThreadPoolBulkheadBeanLoader {


    @InjectMocks
    ThreadPoolBulkheadBeanLoader threadPoolBulkheadBeanLoader;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_defaultConfigs() {
        HashMap<String, Map<String, String>> thConfigs = new HashMap<>();
        HashMap<String, String> thConfig1 = new HashMap<>();
        thConfigs.put("thConfig1", thConfig1);

        ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry = threadPoolBulkheadBeanLoader.threadPoolBulkheadRegistry(thConfigs);

        ThreadPoolBulkheadConfig bulkheadConfig = threadPoolBulkheadRegistry.getConfiguration("thConfig1").get();

        Assertions.assertEquals(Integer.valueOf(CORE_THREAD_POOL_SIZE_DEFAULT), bulkheadConfig.getCoreThreadPoolSize());
        Assertions.assertEquals(Integer.valueOf(MAX_THREAD_POOL_SIZE_DEFAULT), bulkheadConfig.getMaxThreadPoolSize());
        Assertions.assertEquals(Integer.valueOf(QUEUE_CAPACITY_DEFAULT), bulkheadConfig.getQueueCapacity());
        Assertions.assertEquals(Long.valueOf(KEEP_ALIVE_DURATION_DEFAULT), bulkheadConfig.getKeepAliveDuration().toMillis());
        Assertions.assertTrue(bulkheadConfig.getContextPropagator().isEmpty());
    }

    @Test
    public void test_providedConfigs() {

        HashMap<String, Map<String, String>> thConfigs = new HashMap<>();
        HashMap<String, String> thConfig1 = new HashMap<>();
        thConfig1.put(CORE_THREAD_POOL_SIZE, "50");
        thConfig1.put(MAX_THREAD_POOL_SIZE, "99");
        thConfig1.put(QUEUE_CAPACITY, "100");
        thConfig1.put(KEEP_ALIVE_DURATION, "100");
        thConfigs.put("thConfig1", thConfig1);

        ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry = threadPoolBulkheadBeanLoader.threadPoolBulkheadRegistry(thConfigs);

        ThreadPoolBulkheadConfig bulkheadConfig = threadPoolBulkheadRegistry.getConfiguration("thConfig1").get();

        Assertions.assertEquals(Integer.valueOf(50), bulkheadConfig.getCoreThreadPoolSize());
        Assertions.assertEquals(Integer.valueOf(99), bulkheadConfig.getMaxThreadPoolSize());
        Assertions.assertEquals(Integer.valueOf(100), bulkheadConfig.getQueueCapacity());
        Assertions.assertEquals(Long.valueOf(100), bulkheadConfig.getKeepAliveDuration().toMillis());
    }

}
