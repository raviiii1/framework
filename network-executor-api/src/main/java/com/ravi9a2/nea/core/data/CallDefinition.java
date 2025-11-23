package com.ravi9a2.nea.core.data;

/**
 * A generic pojo to hold a call's definition.
 *
 * @author raviprakash
 */
public class CallDefinition {

    private final String id;
    private final String serviceTag;
    private final Type type;
    private final boolean isSilentFailure;
    private final boolean isRetryable;
    private final boolean isCircuitBreakerEnabled;
    private final boolean isBulkheadEnabled;
    private final String bhTag;
    private final String retryTag;
    private final String cbTag;
    private final java.lang.reflect.Type responseType;
    private final Object payload;

    protected CallDefinition(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.isSilentFailure = builder.isSilentFailure;
        this.isRetryable = builder.isRetryable;
        this.serviceTag = builder.serviceTag;
        this.isCircuitBreakerEnabled = builder.isCircuitBreakerEnabled;
        this.isBulkheadEnabled = builder.isBulkheadEnabled;
        this.bhTag = builder.bhTag;
        this.retryTag = builder.retryTag;
        this.cbTag = builder.cbTag;
        this.responseType = builder.responseType;
        this.payload = builder.payload;
    }

    public abstract static class Builder<T> {
        private String id;
        private String serviceTag;
        private Type type;
        private boolean isSilentFailure;
        private boolean isRetryable;
        private boolean isCircuitBreakerEnabled;
        private boolean isBulkheadEnabled;
        private String bhTag;
        private String retryTag;
        private String cbTag;
        private java.lang.reflect.Type responseType;
        private Object payload;

        public T id(String id) {
            this.id = id;
            return this.getThis();
        }

        public T type(Type type) {
            this.type = type;
            return this.getThis();
        }

        public T serviceTag(String serviceTag) {
            this.serviceTag = serviceTag;
            return this.getThis();
        }

        public T isSilentFailure(boolean isSilentFailure) {
            this.isSilentFailure = isSilentFailure;
            return this.getThis();
        }

        public T isRetryable(boolean isRetryable) {
            this.isRetryable = isRetryable;
            return this.getThis();
        }

        public T isCircuitBreakerEnabled(boolean isCircuitBreakerEnabled) {
            this.isCircuitBreakerEnabled = isCircuitBreakerEnabled;
            return this.getThis();
        }

        public T isBulkheadEnabled(boolean isBulkheadEnabled) {
            this.isBulkheadEnabled = isBulkheadEnabled;
            return this.getThis();
        }

        public T retryTag(String retryTag) {
            this.retryTag = retryTag;
            return this.getThis();
        }

        public T bhTag(String bhTag) {
            this.bhTag = bhTag;
            return this.getThis();
        }

        public T cbTag(String cbTag) {
            this.cbTag = cbTag;
            return this.getThis();
        }

        public T responseType(java.lang.reflect.Type responseType) {
            this.responseType = responseType;
            return this.getThis();
        }

        public T payload(Object payload) {
            this.payload = payload;
            return this.getThis();
        }

        public abstract CallDefinition build();

        protected abstract T getThis();
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public boolean isSilentFailure() {
        return isSilentFailure;
    }

    public boolean isRetryable() {
        return isRetryable;
    }

    public boolean isCircuitBreakerEnabled() {
        return isCircuitBreakerEnabled;
    }

    public boolean isBulkheadEnabled() {
        return isBulkheadEnabled;
    }

    public String getBhTag() {
        return bhTag;
    }

    public String getRetryTag() {
        return retryTag;
    }

    public String getCbTag() {
        return cbTag;
    }

    public java.lang.reflect.Type getResponseType() {
        return responseType;
    }

    public Object getPayload() {
        return payload;
    }
}
