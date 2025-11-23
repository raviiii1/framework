package com.ravi9a2.nea.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents an HTTP patch call and wraps the fields of a Rest call definition.
 *
 * @author raviprakash
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface PatchCall {
    String path() default "";

    String service() default "";

    boolean isSilent() default true;

    boolean isRetryable() default false;

    boolean cbEnabled() default false;

    boolean bhEnabled() default false;

    String bulkhead() default "";

    String retry() default "";

    String circuitBreaker() default "";

    String fallback() default "";
}
