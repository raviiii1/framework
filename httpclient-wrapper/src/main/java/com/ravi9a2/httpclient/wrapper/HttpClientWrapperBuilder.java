package com.ravi9a2.httpclient.wrapper;

import com.ravi9a2.nca.ClientBuilder;
import com.ravi9a2.nca.data.ClientConfig;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
public class HttpClientWrapperBuilder implements ClientBuilder<HttpClientWrapper> {

    public static final String HTTP = "http";
    public static final String HTTPS = "https";

    protected String clientName;
    protected String baseUrl;
    protected int maxConnections;
    protected int defaultMaxPerRoute;
    protected int connectTimeout;
    protected long readTimeout;
    protected long writeTimeout;
    protected long socketTimeout;
    protected Map<String, String> headers;

    public HttpClientWrapperBuilder clientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public HttpClientWrapperBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public HttpClientWrapperBuilder maxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public HttpClientWrapperBuilder defaultMaxPerRoute(int defaultMaxPerRoute) {
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        return this;
    }

    public HttpClientWrapperBuilder connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public HttpClientWrapperBuilder socketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }


    public HttpClientWrapperBuilder readTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public HttpClientWrapperBuilder writeTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public HttpClientWrapperBuilder headers(Map<String, String> headers) {
        if (Objects.isNull(this.headers)) {
            this.headers = new HashMap<>();
        }
        if (Objects.nonNull(headers)) {
            this.headers.putAll(headers);
        }
        return this;
    }

    public HttpClientWrapperBuilder header(String key, String value) {
        if (Objects.isNull(this.headers)) {
            this.headers = new HashMap<>();
        }
        if (Objects.nonNull(key) && Objects.nonNull(value)) {
            this.headers.put(key, value);
        }
        return this;
    }

    @Override
    public HttpClientWrapper build() {
        return constructHttpClientWrapper();
    }

    @Override
    public HttpClientWrapper build(ClientConfig cc) {
        this.clientName = cc.getClientName();
        this.baseUrl = cc.getBaseUrl();
        this.maxConnections = cc.getTimeouts().getMaxConnections();
        this.defaultMaxPerRoute = cc.getTimeouts().getDefaultMaxPerRoute();
        this.connectTimeout = cc.getTimeouts().getConnectTimeout();
        this.socketTimeout = cc.getTimeouts().getSocketTimeout();
        this.readTimeout = cc.getTimeouts().getReadTimeout();
        setSpecialHeaders(cc.getAuthentication().getAuthKey(), cc.getAuthentication().getAuthSecret());
        setSpecialHeaders(cc.getAuthentication().getSecondAuthKey(), cc.getAuthentication().getSecondAuthSecret());
        return constructHttpClientWrapper();
    }

    private HttpClientWrapper constructHttpClientWrapper() {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(HTTP, PlainConnectionSocketFactory.getSocketFactory())
                .register(HTTPS, SSLConnectionSocketFactory.getSocketFactory()).build();
        PoolingHttpClientConnectionManager pooledConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        pooledConnectionManager.setMaxTotal(this.maxConnections);
        pooledConnectionManager.setDefaultMaxPerRoute(this.defaultMaxPerRoute);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(this.connectTimeout)
                .setConnectionRequestTimeout((int)this.readTimeout)
                .setSocketTimeout((int)this.socketTimeout)
                .build();
        HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(pooledConnectionManager)
                .addInterceptorLast(new OutBoundInterceptor())
                .build();
        return new HttpClientWrapper(httpClient)
                .headers(this.headers)
                .baseUrl(this.baseUrl);
    }

    private void setSpecialHeaders(String key, String value) {
        if (Objects.isNull(this.headers)) {
            this.headers = new HashMap<>();
        }
        if (Objects.nonNull(key) && Objects.nonNull(value)) {
            this.headers.put(key, value);
        }
    }
}
