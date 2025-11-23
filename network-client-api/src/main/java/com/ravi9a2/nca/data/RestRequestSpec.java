package com.ravi9a2.nca.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RestRequestSpec extends RequestSpec {

    protected Map<String, String> requestParams;
    protected Map<String, String> pathParams;
    protected String url;
    protected String httpMethod;

    private RestRequestSpec(Builder builder) {
        super(builder);
        this.url = builder.url;
        this.pathParams = builder.pathParams;
        this.requestParams = builder.requestParams;
        this.httpMethod = builder.httpMethod;
    }

    public Map<String, String> getRequestParams() {
        return requestParams;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public String getUrl() {
        return url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public static Builder builder() {
        return new RestRequestSpec.Builder();
    }

    public static class Builder extends RequestSpec.Builder<Builder> {
        private Map<String, String> requestParams;
        private Map<String, String> pathParams;
        private String url;
        private String httpMethod;

        public Builder requestParams(Map<String, String> requestParams) {
            if (Objects.isNull(this.requestParams)) {
                this.requestParams = new HashMap<>();
            }
            if (Objects.nonNull(requestParams)) {
                this.requestParams.putAll(requestParams);
            }
            return this;
        }

        public Builder requestParam(String k, String v) {
            if (Objects.isNull(this.requestParams)) {
                this.requestParams = new HashMap<>();
            }
            if (Objects.nonNull(k) && Objects.nonNull(v)) {
                this.requestParams.put(k, v);
            }
            return this;
        }

        public Builder pathParams(Map<String, String> pathParams) {
            if (Objects.isNull(this.pathParams)) {
                this.pathParams = new HashMap<>();
            }
            if (Objects.nonNull(pathParams)) {
                this.pathParams.putAll(pathParams);
            }
            return this;
        }

        public Builder pathParam(String k, String v) {
            if (Objects.isNull(this.pathParams)) {
                this.pathParams = new HashMap<>();
            }
            if (Objects.nonNull(k) && Objects.nonNull(v)) {
                this.pathParams.put(k, v);
            }
            return this;
        }

        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        @Override
        public RestRequestSpec build() {
            return new RestRequestSpec(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
