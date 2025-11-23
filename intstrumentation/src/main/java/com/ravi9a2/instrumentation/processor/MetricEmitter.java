package com.ravi9a2.instrumentation.processor;

import java.util.Map;

/**
 * Generic interface for emitting metrics to various metrics backends.
 * This interface provides a unified API for emitting different types of metrics
 * (counters, gauges, latency/histograms) that can be implemented for different
 * metrics systems (Prometheus, StatsD, CloudWatch, etc.).
 * 
 * <p>
 * Implementations should handle the connection and configuration to the metrics
 * backend. The host service only needs to configure the metrics
 * server/endpoint.
 * 
 * @author raviprakash
 */
public interface MetricEmitter {

    /**
     * Increment a counter metric by 1.
     * 
     * @param metricName Name of the metric (e.g., "http.requests.total")
     * @param tags       Map of key-value pairs for metric tags/labels
     */
    void incrementCounter(String metricName, Map<String, String> tags);

    /**
     * Increment a counter metric by a specific value.
     * 
     * @param metricName Name of the metric
     * @param value      The value to increment by
     * @param tags       Map of key-value pairs for metric tags/labels
     */
    void incrementCounter(String metricName, double value, Map<String, String> tags);

    /**
     * Record a latency/duration metric.
     * 
     * @param metricName Name of the metric (e.g., "http.request.duration")
     * @param latency    Latency in milliseconds
     * @param tags       Map of key-value pairs for metric tags/labels
     */
    void recordLatency(String metricName, long latency, Map<String, String> tags);

    /**
     * Set a gauge metric to a specific value.
     * 
     * @param metricName Name of the metric (e.g., "cache.size")
     * @param value      The gauge value
     * @param tags       Map of key-value pairs for metric tags/labels
     */
    void setGauge(String metricName, double value, Map<String, String> tags);

    /**
     * Increment a gauge metric by a specific value.
     * 
     * @param metricName Name of the metric
     * @param value      The value to increment by
     * @param tags       Map of key-value pairs for metric tags/labels
     */
    void incrementGauge(String metricName, double value, Map<String, String> tags);

    /**
     * Decrement a gauge metric by a specific value.
     * 
     * @param metricName Name of the metric
     * @param value      The value to decrement by
     * @param tags       Map of key-value pairs for metric tags/labels
     */
    void decrementGauge(String metricName, double value, Map<String, String> tags);

    /**
     * Record a histogram value (similar to latency but for any numeric value).
     * 
     * @param metricName Name of the metric (e.g., "response.size")
     * @param value      The value to record
     * @param tags       Map of key-value pairs for metric tags/labels
     */
    void recordHistogram(String metricName, double value, Map<String, String> tags);
}
