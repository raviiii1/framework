package com.ravi9a2.instrumentation.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Default implementation of MetricEmitter that stores metrics in-memory.
 * This is a no-op implementation that can be used for testing or as a fallback.
 * 
 * <p>
 * For production use, implement MetricEmitter with your metrics backend
 * (Prometheus, StatsD, CloudWatch, etc.) or use one of the provided
 * implementations.
 * 
 * @author raviprakash
 */
public class DefaultMetricEmitter implements MetricEmitter {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMetricEmitter.class);

    // In-memory storage for metrics (useful for testing/debugging)
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DoubleAdder> gauges = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> latencies = new ConcurrentHashMap<>();

    @Override
    public void incrementCounter(String metricName, Map<String, String> tags) {
        incrementCounter(metricName, 1.0, tags);
    }

    @Override
    public void incrementCounter(String metricName, double value, Map<String, String> tags) {
        String key = buildKey(metricName, tags);
        counters.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet((long) value);

        if (logger.isTraceEnabled()) {
            logger.trace("Counter increment: {} = {} (tags: {})", metricName, value, tags);
        }
    }

    @Override
    public void recordLatency(String metricName, long latency, Map<String, String> tags) {
        String key = buildKey(metricName, tags);
        latencies.put(key, new AtomicLong(latency));

        if (logger.isTraceEnabled()) {
            logger.trace("Latency recorded: {} = {}ms (tags: {})", metricName, latency, tags);
        }
    }

    @Override
    public void setGauge(String metricName, double value, Map<String, String> tags) {
        String key = buildKey(metricName, tags);
        DoubleAdder gauge = new DoubleAdder();
        gauge.add(value);
        gauges.put(key, gauge);

        if (logger.isTraceEnabled()) {
            logger.trace("Gauge set: {} = {} (tags: {})", metricName, value, tags);
        }
    }

    @Override
    public void incrementGauge(String metricName, double value, Map<String, String> tags) {
        String key = buildKey(metricName, tags);
        gauges.computeIfAbsent(key, k -> new DoubleAdder()).add(value);

        if (logger.isTraceEnabled()) {
            logger.trace("Gauge increment: {} += {} (tags: {})", metricName, value, tags);
        }
    }

    @Override
    public void decrementGauge(String metricName, double value, Map<String, String> tags) {
        String key = buildKey(metricName, tags);
        gauges.computeIfAbsent(key, k -> new DoubleAdder()).add(-value);

        if (logger.isTraceEnabled()) {
            logger.trace("Gauge decrement: {} -= {} (tags: {})", metricName, value, tags);
        }
    }

    @Override
    public void recordHistogram(String metricName, double value, Map<String, String> tags) {
        // For default implementation, histogram is treated similar to latency
        String key = buildKey(metricName, tags);
        latencies.put(key, new AtomicLong((long) value));

        if (logger.isTraceEnabled()) {
            logger.trace("Histogram recorded: {} = {} (tags: {})", metricName, value, tags);
        }
    }

    /**
     * Builds a unique key from metric name and tags.
     */
    private String buildKey(String metricName, Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return metricName;
        }
        StringBuilder sb = new StringBuilder(metricName);
        tags.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sb.append(".").append(entry.getKey()).append("=").append(entry.getValue()));
        return sb.toString();
    }

    /**
     * Gets the current counter value (for testing/debugging).
     */
    public long getCounterValue(String metricName, Map<String, String> tags) {
        String key = buildKey(metricName, tags);
        AtomicLong counter = counters.get(key);
        return counter != null ? counter.get() : 0;
    }

    /**
     * Gets the current gauge value (for testing/debugging).
     */
    public double getGaugeValue(String metricName, Map<String, String> tags) {
        String key = buildKey(metricName, tags);
        DoubleAdder gauge = gauges.get(key);
        return gauge != null ? gauge.sum() : 0.0;
    }

    /**
     * Clears all stored metrics (for testing).
     */
    public void clear() {
        counters.clear();
        gauges.clear();
        latencies.clear();
    }
}
