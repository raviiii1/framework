package com.ravi9a2.nca.data;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RequestSpec {

    protected Map<String, String> headers;
    protected Object body;
    protected Type type;

    protected RequestSpec(Builder builder) {
        this.body = builder.body;
        this.type = builder.type;
        this.headers = builder.headers;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Object getBody() {
        return body;
    }

    public Type getType() {
        return type;
    }

    public abstract static class Builder<T> {
        private Map<String, String> headers;
        private Object body;
        private Type type;

        public T headers(Map<String, String> headers) {
            if (Objects.isNull(this.headers)) {
                this.headers = new HashMap<>();
            }
            if (Objects.nonNull(headers)) {
                this.headers.putAll(headers);
            }
            return this.getThis();
        }

        public T header(String k, String v) {
            if (Objects.isNull(this.headers)) {
                this.headers = new HashMap<>();
            }
            if (Objects.nonNull(k) && Objects.nonNull(v)) {
                this.headers.put(k, v);
            }
            return this.getThis();
        }

        public T body(Object body) {
            this.body = body;
            return this.getThis();
        }

        public T type(Type type) {
            this.type = type;
            return this.getThis();
        }

        public abstract RequestSpec build();

        protected abstract T getThis();
    }
}
