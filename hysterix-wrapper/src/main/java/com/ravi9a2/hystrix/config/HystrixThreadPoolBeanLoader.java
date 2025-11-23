package com.ravi9a2.hystrix.config;

import com.netflix.hystrix.HystrixThreadPoolProperties;
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
 * Thread Pool bean loader configuration class that
 * loads Hystrix thread pool properties from configuration and
 * exposes them via HystrixThreadPoolPropertiesRegistry.
 *
 * @author raviprakash
 */
@Configuration
public class HystrixThreadPoolBeanLoader {

    public static final String DEFAULT = "default";
    public static final String CORE_SIZE = "coreSize";
    public static final String CORE_SIZE_DEFAULT = "10";
    public static final String MAXIMUM_SIZE = "maximumSize";
    public static final String MAXIMUM_SIZE_DEFAULT = "10";
    public static final String MAX_QUEUE_SIZE = "maxQueueSize";
    public static final String MAX_QUEUE_SIZE_DEFAULT = "-1";
    public static final String KEEP_ALIVE_TIME_MINUTES = "keepAliveTimeMinutes";
    public static final String KEEP_ALIVE_TIME_MINUTES_DEFAULT = "1";
    public static final String QUEUE_SIZE_REJECTION_THRESHOLD = "queueSizeRejectionThreshold";
    public static final String QUEUE_SIZE_REJECTION_THRESHOLD_DEFAULT = "5";

    private static final Logger LOGGER = LoggerFactory.getLogger(HystrixThreadPoolBeanLoader.class);
    private static final String TP_SETUP_COMPLETE_MSG = "[Hystrix TP] Thread pool setup complete for: {}";
    private static final String TP_REGISTRY_SETUP_COMPLETE_MSG = "[Hystrix TP] ThreadPoolRegistry setup complete.";

    @Bean("allRawHystrixTPConfigs")
    @ConfigurationProperties(prefix = "hystrix.thread-pool")
    public Map<String, Map<String, String>> allRawHystrixTPConfigs() {
        return new HashMap<>();
    }

    @Bean
    public HystrixThreadPoolPropertiesRegistry hystrixThreadPoolPropertiesRegistry(
            @Qualifier("allRawHystrixTPConfigs") Map<String, Map<String, String>> allRawHystrixTPConfigs) {

        HystrixThreadPoolPropertiesRegistry registry = new HystrixThreadPoolPropertiesRegistry();

        Map<String, HystrixThreadPoolProperties.Setter> allTPConfigs = allRawHystrixTPConfigs.entrySet().stream()
                .filter(e -> Objects.nonNull(e.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> constructHystrixThreadPoolProperties(e.getValue(), allRawHystrixTPConfigs.get(DEFAULT))
                ));

        allTPConfigs.forEach((name, properties) -> {
            registry.register(name, properties);
            LOGGER.info(TP_SETUP_COMPLETE_MSG, name);
        });

        LOGGER.info(TP_REGISTRY_SETUP_COMPLETE_MSG);
        return registry;
    }

    private HystrixThreadPoolProperties.Setter constructHystrixThreadPoolProperties(
            Map<String, String> config, Map<String, String> defaultConfig) {

        HystrixThreadPoolProperties.Setter setter = HystrixThreadPoolProperties.Setter();

        setter.withCoreSize(
                Integer.parseInt(getValue(config, defaultConfig, CORE_SIZE, CORE_SIZE_DEFAULT))
        );

        setter.withMaximumSize(
                Integer.parseInt(getValue(config, defaultConfig, MAXIMUM_SIZE, MAXIMUM_SIZE_DEFAULT))
        );

        int maxQueueSize = Integer.parseInt(getValue(config, defaultConfig, MAX_QUEUE_SIZE, MAX_QUEUE_SIZE_DEFAULT));
        setter.withMaxQueueSize(maxQueueSize);

        setter.withKeepAliveTimeMinutes(
                Integer.parseInt(getValue(config, defaultConfig, KEEP_ALIVE_TIME_MINUTES, KEEP_ALIVE_TIME_MINUTES_DEFAULT))
        );

        setter.withQueueSizeRejectionThreshold(
                Integer.parseInt(getValue(config, defaultConfig, QUEUE_SIZE_REJECTION_THRESHOLD, QUEUE_SIZE_REJECTION_THRESHOLD_DEFAULT))
        );

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

