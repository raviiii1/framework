package com.ravi9a2.webclient.wrapper;

import com.ravi9a2.nca.ClientBuilder;
import com.ravi9a2.nca.data.ClientConfig;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class WebClientWrapperBuilder implements ClientBuilder<WebClientWrapper> {

    protected String clientName;
    protected String baseUrl;
    protected int maxConnections;
    protected int connectTimeout;
    protected long readTimeout;
    protected long writeTimeout;
    protected Map<String, String> headers;
    protected ThreadPoolTaskExecutor webClientTaskExecutor;
    protected int inMemoryBufferSizeInKB;

    public WebClientWrapperBuilder clientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public WebClientWrapperBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public WebClientWrapperBuilder maxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public WebClientWrapperBuilder connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public WebClientWrapperBuilder readTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public WebClientWrapperBuilder writeTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public WebClientWrapperBuilder inMemoryBufferSizeInKB(int inMemoryBufferSize) {
        this.inMemoryBufferSizeInKB = inMemoryBufferSize;
        return this;
    }

    public WebClientWrapperBuilder headers(Map<String, String> map) {
        if (Objects.isNull(this.headers)) {
            this.headers = new HashMap<>();
        }
        if (Objects.nonNull(map)) {
            this.headers.putAll(map);
        }
        return this;
    }

    public WebClientWrapperBuilder header(String key, String value) {
        if (Objects.isNull(this.headers)) {
            this.headers = new HashMap<>();
        }
        if (Objects.nonNull(key) && Objects.nonNull(value)) {
            this.headers.put(key, value);
        }
        return this;
    }

    public WebClientWrapperBuilder webClientTaskExecutor(ThreadPoolTaskExecutor e) {
        this.webClientTaskExecutor = e;
        return this;
    }

    @Override
    public WebClientWrapper build() {
        return constructWebClientWrapper();
    }

    @Override
    public WebClientWrapper build(ClientConfig cc) {
        this.clientName = cc.getClientName();
        this.baseUrl = cc.getBaseUrl();
        this.maxConnections = cc.getTimeouts().getMaxConnections();
        this.connectTimeout = (int) cc.getTimeouts().getConnectTimeout();
        this.readTimeout = cc.getTimeouts().getReadTimeout();
        this.writeTimeout = cc.getTimeouts().getWriteTimeout();
        setSpecialHeaders(cc.getAuthentication().getAuthKey(), cc.getAuthentication().getAuthSecret());
        setSpecialHeaders(cc.getAuthentication().getSecondAuthKey(), cc.getAuthentication().getSecondAuthSecret());
        return constructWebClientWrapper();
    }

    private WebClientWrapper constructWebClientWrapper() {
        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient
                        .create(ConnectionProvider.builder(this.clientName + "-connection-provider")
                                .maxConnections(this.maxConnections)
                                .build())
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.connectTimeout)
                        .doOnConnected(conn -> conn
                                .addHandlerLast(new ReadTimeoutHandler(this.readTimeout, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(this.writeTimeout, TimeUnit.MILLISECONDS))
                        )
                ))
                .baseUrl(this.baseUrl)
                .defaultHeaders(constructHttpHeaders)
                .filter(new ResponseExceptionHandler())
                .codecs(codecs -> codecs
                        .defaultCodecs()
                        .maxInMemorySize(this.inMemoryBufferSizeInKB * 1024))
                .build();

        if (Objects.isNull(webClientTaskExecutor)) {
            return new WebClientWrapper(webClient);
        } else {
            return new WebClientWrapper(webClient, webClientTaskExecutor);
        }
    }

    private void setSpecialHeaders(String key, String value) {
        if (Objects.isNull(this.headers)) {
            this.headers = new HashMap<>();
        }
        if (Objects.nonNull(key) && Objects.nonNull(value)) {
            this.headers.put(key, value);
        }
    }

    private final Consumer<HttpHeaders> constructHttpHeaders = h -> {
        if (Objects.isNull(this.headers)) {
            this.headers = new HashMap<>();
        }
        this.headers.forEach(h::add);
    };

}