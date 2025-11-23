package com.ravi9a2.nea.annotations;

import com.ravi9a2.nea.core.data.RPCMethod;
import com.ravi9a2.nea.core.data.Type;

public @interface ClientStreamRPCCall {

    String path() default "";

    String service() default "";

    boolean isSilent() default true;

    boolean isRetryable() default false;

    boolean cbEnabled() default false;

    boolean bhEnabled() default false;

    RPCMethod method() default RPCMethod.CLIENT_STREAMING;

    Type type() default Type.RPC;

    String bulkhead() default "";

    String retry() default "";

    String circuitBreaker() default "";

    String fallback() default "";

    String fqPackageName() default "";

    String className() default "";

    String methodName() default "";
}
