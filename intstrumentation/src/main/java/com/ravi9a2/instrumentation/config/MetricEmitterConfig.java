package com.ravi9a2.instrumentation.config;

import com.ravi9a2.instrumentation.processor.DefaultMetricEmitter;
import com.ravi9a2.instrumentation.processor.MetricEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration for MetricEmitter.
 * This class provides a default MetricEmitter bean that can be overridden
 * by providing a custom MetricEmitter implementation.
 * 
 * <p>
 * To use a custom metrics backend, create a bean of type MetricEmitter
 * and it will be used instead of the default.
 * 
 * <p>
 * Configuration properties (optional):
 * <ul>
 * <li>instrumentation.metrics.enabled: Enable/disable metrics (default:
 * true)</li>
 * <li>instrumentation.metrics.endpoint: Metrics server endpoint (if
 * applicable)</li>
 * </ul>
 * 
 * @author raviprakash
 */
@Configuration
public class MetricEmitterConfig {

    private static final Logger logger = LoggerFactory.getLogger(MetricEmitterConfig.class);

    @Value("${instrumentation.metrics.enabled:true}")
    private boolean metricsEnabled;

    @Value("${instrumentation.metrics.endpoint:}")
    private String metricsEndpoint;

    /**
     * Creates a default MetricEmitter bean if none is provided.
     * The default implementation is a no-op that logs metrics at trace level.
     * 
     * <p>
     * To use a custom metrics backend, create a bean implementing MetricEmitter:
     * 
     * <pre>
     * {@code
     * @Bean
     * public MetricEmitter customMetricEmitter() {
     *     return new YourCustomMetricEmitter(metricsEndpoint);
     * }
     * }
     * </pre>
     * 
     * @return DefaultMetricEmitter instance
     */
    @Bean
    public MetricEmitter metricEmitter(@Autowired(required = false) MetricEmitter customEmitter) {
        if (customEmitter != null) {
            logger.info("Using custom MetricEmitter: {}", customEmitter.getClass().getName());
            return customEmitter;
        }

        if (!metricsEnabled) {
            logger.info("Metrics are disabled. Using no-op MetricEmitter.");
            return new NoOpMetricEmitter();
        }

        logger.info(
                "Using default MetricEmitter. Configure a custom MetricEmitter bean to emit metrics to your backend.");
        if (metricsEndpoint != null && !metricsEndpoint.isEmpty()) {
            logger.info("Metrics endpoint configured: {}", metricsEndpoint);
        }

        return new DefaultMetricEmitter();
    }

    /**
     * No-op implementation that does nothing.
     */
    private static class NoOpMetricEmitter implements MetricEmitter {
        @Override
        public void incrementCounter(String metricName, Map<String, String> tags) {
            // No-op
        }

        @Override
        public void incrementCounter(String metricName, double value, Map<String, String> tags) {
            // No-op
        }

        @Override
        public void recordLatency(String metricName, long latency, Map<String, String> tags) {
            // No-op
        }

        @Override
        public void setGauge(String metricName, double value, Map<String, String> tags) {
            // No-op
        }

        @Override
        public void incrementGauge(String metricName, double value, Map<String, String> tags) {
            // No-op
        }

        @Override
        public void decrementGauge(String metricName, double value, Map<String, String> tags) {
            // No-op
        }

        @Override
        public void recordHistogram(String metricName, double value, Map<String, String> tags) {
            // No-op
        }
    }
}
