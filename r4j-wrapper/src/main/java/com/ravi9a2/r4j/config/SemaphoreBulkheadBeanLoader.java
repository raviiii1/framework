package com.ravi9a2.r4j.config;

import com.ravi9a2.r4j.Metrics;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
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

import static com.ravi9a2.r4j.Metrics.BH_AVAILABLE_PERMIT_METRIC_NAME;
import static com.ravi9a2.r4j.Metrics.BH_CALL_REJECTED_METRIC_NAME;

/**
 * SemaphoreBulkhead bean loader configuration class that
 * exposes the beans via BulkheadRegistry.
 *
 * @author raviprakash
 */
@Configuration
public class SemaphoreBulkheadBeanLoader {

    public static final String DEFAULT = "default";
    public static final String MAX_CONCURRENT_CALLS = "maxConcurrentCalls";
    public static final String MAX_CONCURRENT_CALLS_DEFAULT = "100";
    public static final String MAX_WAIT_DURATION = "maxWaitDuration";
    public static final String MAX_WAIT_DURATION_DEFAULT = "0";
    public static final String WRITABLE_STACK_TRACE_ENABLED = "writableStackTraceEnabled";
    public static final String FAIR_CALL_HANDLING_ENABLED_DEFAULT = "false";
    public static final String WRITABLE_STACK_TRACE_ENABLED_DEFAULT = "false";
    public static final String FAIR_CALL_HANDLING_ENABLED = "fairCallHandlingEnabled";
    private static final Logger LOGGER = LogManager.getLogger(SemaphoreBulkheadBeanLoader.class);
    private static final String BH_CALL_REJECTED_MSG = "[BH Event] Call rejected by bulkhead name: {}";
    private static final String BH_CALL_FINISHED_MSG = "[BH Event] Call finished, Bulkhead name: {}";
    private static final String BH_SETUP_COMPLETE_MSG = "[BH Event] BulkHeadRegistry setup complete.";
    private static final String BH_REGISTER_MSG = "[BH Event] BulkHeadRegistry register event encountered: {}";

    @Bean("allRawSBHConfigs")
    @ConfigurationProperties(prefix = "r4j.bulkhead")
    public Map<String, Map<String, String>> allRawSBHConfigs() {
        return new HashMap<>();
    }

    @Bean
    public BulkheadRegistry semaphoreBulkheadRegistry(@Qualifier("allRawSBHConfigs") Map<String, Map<String, String>> allRawSBHConfigs) {

        Map<String, BulkheadConfig> allTPBHConfigs = allRawSBHConfigs.entrySet().stream()
                .filter(e -> Objects.nonNull(e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> constructSemaphoreBulkheadConfig(e.getValue(), allRawSBHConfigs.get(DEFAULT))));
        BulkheadRegistry registry = BulkheadRegistry.of(allTPBHConfigs);
        registerEventsOnRegistry(registry);
        registerEvents(allTPBHConfigs, registry);
        return registry;
    }

    private BulkheadConfig constructSemaphoreBulkheadConfig(Map<String, String> c, Map<String, String> d) {
        return BulkheadConfig.custom()
                .maxConcurrentCalls(Integer.parseInt((getValue(c, d, MAX_CONCURRENT_CALLS, MAX_CONCURRENT_CALLS_DEFAULT))))
                .maxWaitDuration(Duration.ofMillis(Long.parseLong(getValue(c, d, MAX_WAIT_DURATION, MAX_WAIT_DURATION_DEFAULT))))
                .writableStackTraceEnabled(Boolean.parseBoolean(getValue(c, d, WRITABLE_STACK_TRACE_ENABLED, WRITABLE_STACK_TRACE_ENABLED_DEFAULT)))
                .fairCallHandlingStrategyEnabled(Boolean.parseBoolean(getValue(c, d, FAIR_CALL_HANDLING_ENABLED, FAIR_CALL_HANDLING_ENABLED_DEFAULT)))
                .build();
    }

    private void registerEvents(Map<String, BulkheadConfig> allTPBHConfigs, BulkheadRegistry registry) {
        allTPBHConfigs.keySet().forEach(key -> {
            Bulkhead bh = registry.bulkhead(key, key);
            bh.getEventPublisher()
                    .onCallRejected(e -> {
                        LOGGER.error(BH_CALL_REJECTED_MSG, e.getBulkheadName());
                        Metrics.increment(BH_CALL_REJECTED_METRIC_NAME, "bulkheadName=" + e.getBulkheadName());
                    })
                    .onCallFinished(e -> {
                        LOGGER.info(BH_CALL_FINISHED_MSG, e.getBulkheadName());
                        Metrics.setGauge(BH_AVAILABLE_PERMIT_METRIC_NAME, getUsedBulkheadCount(bh), "bulkheadName=" + e.getBulkheadName());
                    });
        });
    }

    private int getUsedBulkheadCount(Bulkhead bh) {
        return bh.getMetrics().getMaxAllowedConcurrentCalls() - bh.getMetrics().getAvailableConcurrentCalls();
    }

    private void registerEventsOnRegistry(BulkheadRegistry registry) {
        registry.getEventPublisher()
                .onEvent(e -> LOGGER.info(BH_REGISTER_MSG, e.getEventType().name()));
        LOGGER.info(BH_SETUP_COMPLETE_MSG);
    }

    private String getValue(Map<String, String> c, Map<String, String> d, String k, String v) {
        if (Objects.isNull(d))
            d = Collections.emptyMap();
        if (Objects.isNull(c))
            c = Collections.emptyMap();
        return c.getOrDefault(k, d.getOrDefault(k, v));
    }
}
