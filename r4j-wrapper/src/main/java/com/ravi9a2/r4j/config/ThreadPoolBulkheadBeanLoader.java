package com.ravi9a2.r4j.config;

import com.ravi9a2.r4j.Metrics;
import io.github.resilience4j.bulkhead.ContextPropagator;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
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

import static com.ravi9a2.r4j.Metrics.BH_CALL_REJECTED_METRIC_NAME;
import static com.ravi9a2.r4j.Metrics.TPBH_CORE_SIZE_METRIC_NAME;
import static com.ravi9a2.r4j.Metrics.TPBH_CURRENT_SIZE_METRIC_NAME;
import static com.ravi9a2.r4j.Metrics.TPBH_MAX_SIZE_METRIC_NAME;
import static com.ravi9a2.r4j.Metrics.TPBH_QUEUE_CAPACITY_METRIC_NAME;
import static com.ravi9a2.r4j.Metrics.TPBH_QUEUE_DEPTH_METRIC_NAME;
import static com.ravi9a2.r4j.Metrics.TPBH_REMAINING_QUEUE_CAPACITY_METRIC_NAME;

/**
 * ThreadPoolBulkhead bean loader configuration class that
 * exposes the beans via ThreadPoolBulkheadRegistry.
 *
 * @author raviprakash
 */
@Configuration
public class ThreadPoolBulkheadBeanLoader {
    public static final String DEFAULT = "default";
    public static final String CORE_THREAD_POOL_SIZE = "coreThreadPoolSize";
    public static final String CORE_THREAD_POOL_SIZE_DEFAULT = "100";
    public static final String MAX_THREAD_POOL_SIZE = "maxThreadPoolSize";
    public static final String MAX_THREAD_POOL_SIZE_DEFAULT = "100";
    public static final String QUEUE_CAPACITY = "queueCapacity";
    public static final String QUEUE_CAPACITY_DEFAULT = "100";
    public static final String CONTEXT_PROPAGATOR = "contextPropagator";
    public static final String CONTEXT_PROPAGATOR_DEFAULT = "";
    public static final String KEEP_ALIVE_DURATION = "keepAliveDuration";
    public static final String KEEP_ALIVE_DURATION_DEFAULT = "100";
    public static final String TP_BH_SETUP_COMPLETE_MSG = "[TPBH Event] ThreadPoolBulkHead setup complete.";
    public static final String TP_BH_CALL_FINISHED_MSG = "[TPBH CallFinished] ThreadPoolBulkHead name: {}";
    public static final String TP_BH_CALL_REJECTED_MSG = "[TPBH CallRejected] ThreadPoolBulkHead name: {}";
    public static final String TP_BH_REGISTRY_EVENT_MSG = "[TPBH Event] Registry event encountered: {}";
    public static final String TP_BH_REGISTRY_SETUP_COMPLETE = "[TPBH Event] ThreadPoolBulkHeadRegistry setup complete.";
    private static final Logger LOGGER = LogManager.getLogger(ThreadPoolBulkheadBeanLoader.class);

    @Bean("allRawTPBHConfigs")
    @ConfigurationProperties(prefix = "r4j.tp-bulkhead")
    public Map<String, Map<String, String>> allRawTPBHConfigs() {
        return new HashMap<>();
    }

    @Bean
    public ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry(@Qualifier("allRawTPBHConfigs") Map<String, Map<String, String>> allRawTPBHConfigs) {

        Map<String, ThreadPoolBulkheadConfig> allTPBHConfigs = allRawTPBHConfigs.entrySet().stream()
                .filter(e -> Objects.nonNull(e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> constructThreadPoolBulkheadConfig(e.getValue(), allRawTPBHConfigs.get(DEFAULT))));
        ThreadPoolBulkheadRegistry registry = ThreadPoolBulkheadRegistry.of(allTPBHConfigs);
        registerEventsOnRegistry(registry);
        registerEvents(allTPBHConfigs, registry);
        return registry;
    }

    private ThreadPoolBulkheadConfig constructThreadPoolBulkheadConfig(Map<String, String> c, Map<String, String> d) {
        ThreadPoolBulkheadConfig.Builder builder = ThreadPoolBulkheadConfig.custom()
                .coreThreadPoolSize(Integer.parseInt((getValue(c, d, CORE_THREAD_POOL_SIZE, CORE_THREAD_POOL_SIZE_DEFAULT))))
                .maxThreadPoolSize(Integer.parseInt(getValue(c, d, MAX_THREAD_POOL_SIZE, MAX_THREAD_POOL_SIZE_DEFAULT)))
                .queueCapacity(Integer.parseInt((getValue(c, d, QUEUE_CAPACITY, QUEUE_CAPACITY_DEFAULT))))
                .keepAliveDuration(Duration.ofMillis(Long.parseLong(getValue(c, d, KEEP_ALIVE_DURATION, KEEP_ALIVE_DURATION_DEFAULT))));

        String contextPropagatorClass = getValue(c, d, CONTEXT_PROPAGATOR, CONTEXT_PROPAGATOR_DEFAULT);
        if (!contextPropagatorClass.isEmpty()) {
            try {
                Class<? extends ContextPropagator> aClass = (Class<? extends ContextPropagator>) Class.forName(contextPropagatorClass);
                builder.contextPropagator(aClass);
            } catch (ClassNotFoundException e) {
                LOGGER.error("ContextPropagator ClassNotFoundException {}", e.getStackTrace());
            }
        }
        return builder.build();
    }

    private void registerEvents(Map<String, ThreadPoolBulkheadConfig> allTPBHConfigs, ThreadPoolBulkheadRegistry registry) {
        allTPBHConfigs.keySet().forEach(key -> {
            ThreadPoolBulkhead tbh = registry.bulkhead(key, key);
            LOGGER.info(TP_BH_SETUP_COMPLETE_MSG);
            tbh.getEventPublisher()
                    .onCallFinished(event -> {
                        LOGGER.info(TP_BH_CALL_FINISHED_MSG, event.getBulkheadName());
                        emitThreadPoolBHMetrics(event.getBulkheadName(), tbh);
                    })
                    .onCallRejected(event -> {
                        LOGGER.error(TP_BH_CALL_REJECTED_MSG, event.getBulkheadName());
                        Metrics.increment(BH_CALL_REJECTED_METRIC_NAME, "bulkheadName=" + event.getBulkheadName());
                    });
        });
        // TODO - publish events as metrics
    }

    private void emitThreadPoolBHMetrics(String bulkheadName, ThreadPoolBulkhead tbh) {
        Metrics.setGauge(TPBH_CORE_SIZE_METRIC_NAME, tbh.getMetrics().getCoreThreadPoolSize(), "bulkheadName=" + bulkheadName);
        Metrics.setGauge(TPBH_CURRENT_SIZE_METRIC_NAME, tbh.getMetrics().getThreadPoolSize(), "bulkheadName=" + bulkheadName);
        Metrics.setGauge(TPBH_MAX_SIZE_METRIC_NAME, tbh.getMetrics().getMaximumThreadPoolSize(), "bulkheadName=" + bulkheadName);
        Metrics.setGauge(TPBH_QUEUE_DEPTH_METRIC_NAME, tbh.getMetrics().getQueueDepth(), "bulkheadName=" + bulkheadName);
        Metrics.setGauge(TPBH_REMAINING_QUEUE_CAPACITY_METRIC_NAME, tbh.getMetrics().getRemainingQueueCapacity(), "bulkheadName=" + bulkheadName);
        Metrics.setGauge(TPBH_QUEUE_CAPACITY_METRIC_NAME, tbh.getMetrics().getQueueCapacity(), "bulkheadName=" + bulkheadName);
    }

    private void registerEventsOnRegistry(ThreadPoolBulkheadRegistry registry) {
        registry.getEventPublisher().onEvent(registryEvent -> {
            LOGGER.info(TP_BH_REGISTRY_EVENT_MSG, registryEvent.getEventType().name());
        });
        LOGGER.info(TP_BH_REGISTRY_SETUP_COMPLETE);
    }

    private String getValue(Map<String, String> c, Map<String, String> d, String k, String v) {
        if (Objects.isNull(d))
            d = Collections.emptyMap();
        if (Objects.isNull(c))
            c = Collections.emptyMap();
        return c.getOrDefault(k, d.getOrDefault(k, v));
    }

}
