package com.ravi9a2.r4j.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class FallbackExecutorBeanLoader {

    public static final String FALLBACK_TASK_EXECUTOR = "FALLBACK_TASK_EXECUTOR";
    @Value("${r4j.fallback.core-pool-size:0}")
    private Integer corePoolSize;
    @Value("${r4j.fallback.maximum-pool-size:1}")
    private Integer maximumPoolSize;
    @Value("${r4j.fallback.queue-capacity:1}")
    private Integer queueCapacity;

    @Bean(name = FALLBACK_TASK_EXECUTOR)
    public ThreadPoolExecutor getFallbackTPExecutor() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(queueCapacity)
        );
    }
}
