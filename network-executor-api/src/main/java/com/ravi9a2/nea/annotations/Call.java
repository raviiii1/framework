package com.ravi9a2.nea.annotations;

import com.ravi9a2.nea.core.data.HTTPMethod;
import com.ravi9a2.nea.core.data.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a generic call and wraps the fields of a call definition.
 *
 * @author raviprakash
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Call {
    String path() default "";

    String service() default "";

    boolean isSilent() default true;

    boolean isRetryable() default false;

    boolean cbEnabled() default false;

    boolean bhEnabled() default false;

    HTTPMethod method() default HTTPMethod.POST;

    Type type() default Type.HTTP;

    String bulkhead() default "";

    String retry() default "";

    String circuitBreaker() default "";

    String fallback() default "";
}
