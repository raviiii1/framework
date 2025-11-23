package com.ravi9a2.example.config;

import com.ravi9a2.instrumentation.processor.MetricEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Example implementation of MetricEmitter that logs metrics to console.
 * In a real application, you would implement this to send metrics to
 * your metrics backend (Prometheus, CloudWatch, StatsD, etc.).
 * 
 * @author raviprakash
 */
@Component
@Slf4j
public class ExampleMetricEmitter implements MetricEmitter {

    @Value("${instrumentation.metrics.endpoint:console}")
    private String metricsEndpoint;

    // In-memory storage for demonstration
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DoubleAdder> gauges = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> latencies = new ConcurrentHashMap<>();

    public ExampleMetricEmitter() {
        log.info("Initialized ExampleMetricEmitter with endpoint: {}", metricsEndpoint);
        log.info("Metrics will be logged to console. Implement your own MetricEmitter for production use.");
    }

    @Override
    public void incrementCounter(String metricName, Map<String, String> tags) {
        incrementCounter(metricName, 1.0, tags);
    }

    @Override
    public void incrementCounter(String metricName, double value, Map<String, String> tags) {
        String key = buildKey(metricName, tags);
        counters.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet((long) value);
        log.info("📊 COUNTER: {} = {} (tags: {})", metricName, value, formatTags(tags));
    }

    @Override
    public void recordLatency(String metricName, long latency, Map<String, String> tags) {
        String key = buildKey(metricName, tags);
        latencies.put(key, new AtomicLong(latency));
        log.info("⏱️  LATENCY: {} = {}ms (tags: {})", metricName, latency, formatTags(tags));
    }

    @Override
    public void setGauge(String metricName, double value, Map<String, String> tags) {
        String key = buildKey(metricName, tags);
        DoubleAdder gauge = new DoubleAdder();
        gauge.add(value);
        gauges.put(key, gauge);
        log.info("📈 GAUGE SET: {} = {} (tags: {})", metricName, value, formatTags(tags));
    }

    @Override
    public void incrementGauge(String metricName, double value, Map<String, String> tags) {
        String key = buildKey(metricName, tags);
        gauges.computeIfAbsent(key, k -> new DoubleAdder()).add(value);
        log.info("📈 GAUGE INCREMENT: {} += {} (tags: {})", metricName, value, formatTags(tags));
    }

    @Override
    public void decrementGauge(String metricName, double value, Map<String, String> tags) {
        String key = buildKey(metricName, tags);
        gauges.computeIfAbsent(key, k -> new DoubleAdder()).add(-value);
        log.info("📈 GAUGE DECREMENT: {} -= {} (tags: {})", metricName, value, formatTags(tags));
    }

    @Override
    public void recordHistogram(String metricName, double value, Map<String, String> tags) {
        String key = buildKey(metricName, tags);
        latencies.put(key, new AtomicLong((long) value));
        log.info("📊 HISTOGRAM: {} = {} (tags: {})", metricName, value, formatTags(tags));
    }

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

    private String formatTags(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Get metrics summary (for demonstration purposes).
     */
    public void printMetricsSummary() {
        log.info("=== METRICS SUMMARY ===");
        log.info("Counters: {}", counters.size());
        counters.forEach((key, value) -> log.info("  {} = {}", key, value.get()));
        log.info("Gauges: {}", gauges.size());
        gauges.forEach((key, value) -> log.info("  {} = {}", key, value.sum()));
        log.info("Latencies: {}", latencies.size());
        latencies.forEach((key, value) -> log.info("  {} = {}ms", key, value.get()));
        log.info("======================");
    }
}
