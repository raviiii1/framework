package com.ravi9a2.nea.core.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A pojo extending CallDefinition to hold REST specific call definition.
 *
 * @author raviprakash
 */
public class RestCallDefinition extends CallDefinition {

    private String path;
    private HTTPMethod httpMethod;
    private Map<String, String> httpHeaders;
    private Map<String, String> pathParams;
    private Map<String, String> queryParams;

    public static Builder builder() {
        return new Builder();
    }

    protected RestCallDefinition(Builder builder) {
        super(builder);
        this.path = builder.path;
        this.httpMethod = builder.httpMethod;
        this.httpHeaders = builder.httpHeaders;
        this.pathParams = builder.pathParams;
        this.queryParams = builder.queryParams;
    }

    public String getPath() {
        return path;
    }

    public HTTPMethod getHttpMethod() {
        return httpMethod;
    }

    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public static class Builder extends CallDefinition.Builder<Builder> {
        private String path;
        private HTTPMethod httpMethod;
        private Map<String, String> httpHeaders;
        private Map<String, String> pathParams;
        private Map<String, String> queryParams;

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder httpMethod(HTTPMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder httpHeaders(Map<String, String> httpHeaders) {
            this.httpHeaders = mergeMap(this.httpHeaders, httpHeaders);
            return this;
        }

        public Builder pathParams(Map<String, String> pathParams) {
            this.pathParams = mergeMap(this.pathParams, pathParams);
            return this;
        }

        public Builder queryParams(Map<String, String> queryParams) {
            this.queryParams = mergeMap(this.queryParams, queryParams);
            return this;
        }

        @Override
        public RestCallDefinition build() {
            return new RestCallDefinition(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        private Map<String, String> mergeMap(Map<String, String> original, Map<String, String> map) {
            if (Objects.nonNull(map)) {
                if (Objects.isNull(original)) {
                    original = new HashMap<>();
                }
                original.putAll(map);
            }
            return original;
        }
    }
}
