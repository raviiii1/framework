package com.ravi9a2.nca.data;

/**
 * A pojo holding the timeouts for the Client.
 *
 * @author raviiii1
 */
public class Timeouts {
    protected int defaultMaxPerRoute;
    protected int maxConnections;
    protected int connectTimeout;
    protected long readTimeout;
    protected long writeTimeout;
    protected int socketTimeout;
    private Timeouts(int maxConnections,
                     int defaultMaxPerRoute,
                     int connectTimeout,
                     long readTimeout,
                     long writeTimeout,
                     int socketTimeout) {
        this.maxConnections = maxConnections;
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.socketTimeout = socketTimeout;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getDefaultMaxPerRoute() {return defaultMaxPerRoute;}

    public long getReadTimeout() {
        return readTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public int getSocketTimeout() { return  socketTimeout;}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int defaultMaxPerRoute;
        private int maxConnections;
        private int connectTimeout;
        private long readTimeout;
        private long writeTimeout;
        private int socketTimeout;

        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder defaultMaxPerRoute(int defaultMaxPerRoute) {
            this.defaultMaxPerRoute = defaultMaxPerRoute;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(long readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder socketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public Builder writeTimeout(long writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        public Timeouts build() {
            return new Timeouts(
                    this.maxConnections,
                    this.defaultMaxPerRoute,
                    this.connectTimeout,
                    this.readTimeout,
                    this.writeTimeout,
                    this.socketTimeout
            );
        }
    }
}
