package com.ravi9a2.nca.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A pojo to hold a Client's configuration.
 *
 * @author raviiii1
 */
public class ClientConfig {

    protected String clientName;
    protected Timeouts timeouts;
    protected String baseUrl;
    protected Authentication authentication;
    protected Map<String, String> headers;
    protected int inMemoryBufferSizeInKB;

    protected ClientConfig(Builder builder) {
        this.clientName = builder.clientName;
        this.timeouts = builder.timeouts;
        this.authentication = builder.authentication;
        this.baseUrl = builder.baseUrl;
        this.headers = builder.headers;
        this.inMemoryBufferSizeInKB = builder.inMemoryBufferSizeInKB;
    }

    public String getClientName() {
        return clientName;
    }

    public Timeouts getTimeouts() {
        return timeouts;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getInMemoryBufferSizeInKB() {
        return inMemoryBufferSizeInKB;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String clientName;
        private Timeouts timeouts;
        private String baseUrl;
        private Authentication authentication;
        private Map<String, String> headers;
        private int inMemoryBufferSizeInKB;

        public Builder clientName(String clientName) {
            this.clientName = clientName;
            return this;
        }

        public Builder timeouts(Timeouts timeouts) {
            this.timeouts = timeouts;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder authentication(Authentication authentication) {
            this.authentication = authentication;
            return this;
        }

        public Builder headers(Map<String, String> map) {
            if (Objects.isNull(this.headers)) {
                this.headers = new HashMap<>();
            }
            if (Objects.nonNull(map)) {
                this.headers.putAll(map);
            }
            return this;
        }

        public Builder header(String key, String value) {
            if (Objects.isNull(this.headers)) {
                this.headers = new HashMap<>();
            }
            if (Objects.nonNull(key) && Objects.nonNull(value)) {
                this.headers.put(key, value);
            }
            return this;
        }

        public Builder inMemoryBufferSizeInKB(int inMemoryBufferSizeInKB) {
            this.inMemoryBufferSizeInKB = inMemoryBufferSizeInKB;
            return this;
        }

        public ClientConfig build() {
            return new ClientConfig(this);
        }
    }
}
