package com.ravi9a2.hystrix.config;

import com.netflix.hystrix.HystrixThreadPoolProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Hystrix thread pool properties configurations.
 * Similar to BulkheadRegistry in R4J.
 *
 * @author raviprakash
 */
public class HystrixThreadPoolPropertiesRegistry {

    private final Map<String, HystrixThreadPoolProperties.Setter> threadPoolPropertiesMap;

    public HystrixThreadPoolPropertiesRegistry() {
        this.threadPoolPropertiesMap = new ConcurrentHashMap<>();
    }

    public void register(String name, HystrixThreadPoolProperties.Setter properties) {
        threadPoolPropertiesMap.put(name, properties);
    }

    public HystrixThreadPoolProperties.Setter getThreadPoolProperties(String name) {
        return threadPoolPropertiesMap.getOrDefault(name, getDefaultThreadPoolProperties());
    }

    public HystrixThreadPoolProperties.Setter getDefaultThreadPoolProperties() {
        return HystrixThreadPoolProperties.Setter()
                .withCoreSize(10)
                .withMaximumSize(10)
                .withMaxQueueSize(-1)
                .withKeepAliveTimeMinutes(1);
    }
}
