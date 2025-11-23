package com.ravi9a2.webclient.wrapper;

import com.ravi9a2.nca.exceptions.NetworkClientException;
import com.ravi9a2.nca.exceptions.Status4XXException;
import com.ravi9a2.nca.exceptions.Status5XXException;
import com.ravi9a2.nca.exceptions.TimeoutException;
import com.ravi9a2.webclient.Metrics;
import io.netty.channel.ConnectTimeoutException;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.UnknownHostException;

public class ResponseExceptionHandler implements ExchangeFilterFunction {
    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return next.exchange(request)
                .doOnEach(signal -> {
                    if (signal.isOnNext()) {
                        ClientResponse response = signal.get();
                        Metrics.increment("HTTP_STATUS", "uri=" + getBasePath(request) + "," +
                                "scheme=" + request.url().getScheme() + ",host=" + request.url().getHost() + "," +
                                "method=" + request.method() + ",statusCode=" + response.rawStatusCode() + "," +
                                "statusSeries=" + response.statusCode().series().name());
                        if (response.statusCode().is4xxClientError()) {
                            throw new Status4XXException(getBasePath(request) + " returned " + response.statusCode().getReasonPhrase(), response.rawStatusCode());
                        } else if (response.statusCode().is5xxServerError()) {
                            throw new Status5XXException(getBasePath(request) + " returned " + response.statusCode().getReasonPhrase(), response.rawStatusCode());
                        }
                    }
                }).onErrorMap(th -> {
                    if (th instanceof io.netty.handler.timeout.TimeoutException || th instanceof ConnectTimeoutException) {
                        th = new TimeoutException(th);
                    } else if (th instanceof UnknownHostException) {
                        th = new Status4XXException("Unknown host for path: " + getBasePath(request), 404);
                    } else if (!(th instanceof Status4XXException || th instanceof Status5XXException)) {
                        th = new NetworkClientException(th);
                    }
                    return th;
                });

    }
    
    private String getBasePath(ClientRequest request) {
        try {
            return String.valueOf(request.attribute("org.springframework.web.reactive.function.client.WebClient.uriTemplate").get());
        } catch(Exception e) {
            return request.url().getPath();
        }
    }
}
