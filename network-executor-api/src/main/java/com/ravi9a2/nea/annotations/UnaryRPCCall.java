package com.ravi9a2.nea.annotations;

import com.ravi9a2.nea.core.data.RPCMethod;
import com.ravi9a2.nea.core.data.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface UnaryRPCCall {

    String path() default "";

    String service() default "";

    boolean isSilent() default true;

    boolean isRetryable() default false;

    boolean cbEnabled() default false;

    boolean bhEnabled() default false;

    RPCMethod method() default RPCMethod.UNARY;

    Type type() default Type.RPC;

    String bulkhead() default "";

    String retry() default "";

    String circuitBreaker() default "";

    String fallback() default "";

    String fqPackageName() default "";

    String className() default "";

    String methodName() default "";
}
