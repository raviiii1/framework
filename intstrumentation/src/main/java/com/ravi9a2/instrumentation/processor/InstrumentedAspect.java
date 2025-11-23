package com.ravi9a2.instrumentation.processor;

import com.ravi9a2.instrumentation.annotation.Instrumented;
import com.ravi9a2.instrumentation.enums.MetricType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect that processes {@link Instrumented} annotations at runtime.
 * This aspect intercepts method invocations annotated with @Instrumented
 * and collects execution metrics including latency, success/failure rates, etc.
 * 
 * <p>
 * The aspect extracts the metricType and tagSet from the annotation and
 * emits metrics accordingly. Metrics can be customized by implementing
 * the {@link MetricEmitter} interface.
 * 
 * @author raviprakash
 */
@Aspect
@Component
public class InstrumentedAspect {

    private static final Logger logger = LoggerFactory.getLogger(InstrumentedAspect.class);

    private static final String METRIC_PREFIX = "instrumented";
    private static final String LATENCY_METRIC = METRIC_PREFIX + ".latency";

    private final MetricEmitter metricEmitter;

    /**
     * Constructor with default no-op metric emitter.
     */
    public InstrumentedAspect() {
        this.metricEmitter = new DefaultMetricEmitter();
    }

    /**
     * Constructor with custom metric emitter.
     * 
     * @param metricEmitter Custom metric emitter implementation
     */
    public InstrumentedAspect(MetricEmitter metricEmitter) {
        this.metricEmitter = metricEmitter != null ? metricEmitter : new DefaultMetricEmitter();
    }

    /**
     * Around advice that intercepts methods annotated with @Instrumented.
     * 
     * @param joinPoint The join point representing the method invocation
     * @return The result of the method invocation
     * @throws Throwable If the method throws an exception
     */
    @Around("@annotation(com.ravi9a2.instrumentation.annotation.Instrumented)")
    public Object processInstrumented(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Instrumented instrumented = method.getAnnotation(Instrumented.class);

        if (instrumented == null) {
            // Should not happen, but handle gracefully
            return joinPoint.proceed();
        }

        MetricType metricType = instrumented.metricType();
        String tagSet = instrumented.tagSet();
        Map<String, String> tags = parseTags(tagSet);

        // Add method name and class name to tags
        tags.put("method", method.getName());
        tags.put("class", joinPoint.getTarget().getClass().getSimpleName());
        tags.put("metricType", metricType.name());

        long startTime = System.currentTimeMillis();
        boolean success = false;
        Throwable exception = null;

        try {
            Object result = joinPoint.proceed();
            success = true;
            tags.put("status", String.valueOf(success));
            return result;
        } catch (Throwable t) {
            success = false;
            tags.put("exception", t.getClass().getSimpleName());
            tags.put("status", String.valueOf(success));
            throw t;
        } finally {
            long latency = System.currentTimeMillis() - startTime;
            metricEmitter.recordLatency(LATENCY_METRIC, latency, tags);
            if (logger.isDebugEnabled()) {
                logger.debug("Instrumented: metricType={}, tags={}, latency={}ms, success={}",
                        metricType, tagSet, latency, success);
            }
        }
    }

    /**
     * Parses the tagSet string into a map of key-value pairs.
     * 
     * @param tagSet Comma-separated key-value pairs: "key1=value1,key2=value2"
     * @return Map of tags
     */
    private Map<String, String> parseTags(String tagSet) {
        Map<String, String> tags = new HashMap<>();

        if (tagSet == null || tagSet.trim().isEmpty()) {
            return tags;
        }

        String[] pairs = tagSet.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                tags.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        return tags;
    }

}
