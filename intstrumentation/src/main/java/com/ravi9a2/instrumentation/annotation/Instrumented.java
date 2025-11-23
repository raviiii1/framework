package com.ravi9a2.instrumentation.annotation;

import com.ravi9a2.instrumentation.enums.MetricType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for instrumenting methods for metric collection.
 * This annotation is used to mark methods that should have their execution
 * metrics collected for monitoring and observability purposes.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>
 * {@code @Instrumented(metricType = MetricType.HTTP, tagSet = "path=/api/users,method=getUser")}
 * public User getUser(String id) {
 *     // method implementation
 * }
 * </pre>
 * 
 * @author raviprakash
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Instrumented {

    /**
     * The type of metric being collected.
     * 
     * @return the metric type
     */
    MetricType metricType() default MetricType.GENERIC;

    /**
     * A comma-separated string of key-value pairs representing tags for the metric.
     * Format: "key1=value1,key2=value2,key3=value3"
     * 
     * <p>
     * Example: "path=/api/users,method=getUser,service=user-service"
     * 
     * @return the tag set as a string
     */
    String tagSet() default "";
}
