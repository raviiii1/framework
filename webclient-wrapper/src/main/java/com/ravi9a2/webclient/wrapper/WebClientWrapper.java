package com.ravi9a2.webclient.wrapper;

import com.ravi9a2.nca.ReactiveClient;
import com.ravi9a2.nca.data.RestRequestSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WebClientWrapper implements ReactiveClient<WebClient> {
    private final Logger logger = LoggerFactory.getLogger(WebClientWrapper.class);
    protected WebClient webClient;
    private ThreadPoolTaskExecutor webClientTaskExecutor;

    protected WebClientWrapper(WebClient client) {
        webClient = client;
    }

    protected WebClientWrapper(WebClient client, ThreadPoolTaskExecutor webClientTaskExecutor) {
        this.webClient = client;
        this.webClientTaskExecutor = webClientTaskExecutor;
    }

    @Override
    public <R> Mono<R> callToMono(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec.getHttpMethod(), "HttpMethod can not be null");
        switch (RestRequestSpec.getHttpMethod()) {
            case "POST":
                return postToMono(RestRequestSpec);
            case "GET":
                return getToMono(RestRequestSpec);
            case "DELETE":
                return deleteToMono(RestRequestSpec);
            case "PUT":
                return putToMono(RestRequestSpec);
            case "PATCH":
                return patchToMono(RestRequestSpec);
            default:
                throw new IllegalArgumentException("Invalid http method.");
        }
    }

    @Override
    public <R> Flux<R> callToFlux(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec.getHttpMethod(), "HttpMethod can not be null");
        switch (RestRequestSpec.getHttpMethod()) {
            case "POST":
                return postToFlux(RestRequestSpec);
            case "GET":
                return getToFlux(RestRequestSpec);
            case "DELETE":
                return deleteToFlux(RestRequestSpec);
            case "PUT":
                return putToFlux(RestRequestSpec);
            case "PATCH":
                return patchToFlux(RestRequestSpec);
            default:
                throw new IllegalArgumentException("Invalid http method.");
        }
    }

    @Override
    public <R> Mono<R> postToMono(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec, "Response Type can not be null");
        return toMono(() -> this.post(RestRequestSpec), RestRequestSpec.getType());
    }

    @Override
    public <R> Mono<R> putToMono(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec, "Response Type can not be null");
        return toMono(() -> this.put(RestRequestSpec), RestRequestSpec.getType());
    }

    @Override
    public <R> Mono<R> getToMono(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec, "Response Type can not be null");
        return toMono(() -> this.get(RestRequestSpec), RestRequestSpec.getType());
    }

    @Override
    public <R> Mono<R> deleteToMono(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec, "Response Type can not be null");
        return toMono(() -> this.delete(RestRequestSpec), RestRequestSpec.getType());
    }

    @Override
    public <R> Mono<R> optionsToMono(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec, "Response Type can not be null");
        return toMono(() -> this.options(RestRequestSpec), RestRequestSpec.getType());
    }

    @Override
    public <R> Mono<R> patchToMono(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec, "Response Type can not be null");
        return toMono(() -> this.patch(RestRequestSpec), RestRequestSpec.getType());
    }

    @Override
    public <R> Flux<R> postToFlux(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec, "Response Type can not be null");
        return toFlux(() -> this.post(RestRequestSpec), RestRequestSpec.getType());
    }

    @Override
    public <R> Flux<R> putToFlux(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec, "Response Type can not be null");
        return toFlux(() -> this.put(RestRequestSpec), RestRequestSpec.getType());
    }

    @Override
    public <R> Flux<R> getToFlux(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec, "Response Type can not be null");
        return toFlux(() -> this.get(RestRequestSpec), RestRequestSpec.getType());
    }

    @Override
    public <R> Flux<R> deleteToFlux(RestRequestSpec RestRequestSpec) {
        return toFlux(() -> this.delete(RestRequestSpec), RestRequestSpec.getType());
    }

    @Override
    public <R> Flux<R> optionsToFlux(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec, "Response Type can not be null");
        return toFlux(() -> this.options(RestRequestSpec), RestRequestSpec.getType());
    }

    @Override
    public <R> Flux<R> patchToFlux(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec, "Response Type can not be null");
        return toFlux(() -> this.patch(RestRequestSpec), RestRequestSpec.getType());
    }

    protected WebClientWrapper webClientTaskExecutor(ThreadPoolTaskExecutor e) {
        this.webClientTaskExecutor = e;
        return this;
    }

    private WebClient.ResponseSpec get(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec.getUrl(), "Request URL can not be null");
        return webClient
                .get()
                .uri(RestRequestSpec.getUrl(), getUriVariables(RestRequestSpec))
                .headers(getHttpHeadersConsumer(RestRequestSpec))
                .retrieve();
    }

    private WebClient.ResponseSpec post(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec.getUrl(), "Request URL can not be null");
        Assert.notNull(RestRequestSpec.getBody(), "Request Body can not be null");
        return webClient
                .post()
                .uri(RestRequestSpec.getUrl(), getUriVariables(RestRequestSpec))
                .bodyValue(RestRequestSpec.getBody())
                .headers(getHttpHeadersConsumer(RestRequestSpec))
                .retrieve();
    }

    private WebClient.ResponseSpec put(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec.getUrl(), "Request URL can not be null");
        Assert.notNull(RestRequestSpec.getBody(), "Request Body can not be null");
        return webClient
                .put()
                .uri(RestRequestSpec.getUrl(), getUriVariables(RestRequestSpec))
                .bodyValue(RestRequestSpec.getBody())
                .headers(getHttpHeadersConsumer(RestRequestSpec))
                .retrieve();
    }

    private WebClient.ResponseSpec delete(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec.getUrl(), "Request URL can not be null");
        return webClient
                .delete()
                .uri(RestRequestSpec.getUrl(), getUriVariables(RestRequestSpec))
                .headers(getHttpHeadersConsumer(RestRequestSpec))
                .retrieve();
    }

    private WebClient.ResponseSpec options(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec.getUrl(), "Request URL can not be null");
        return webClient
                .options()
                .uri(RestRequestSpec.getUrl(), getUriVariables(RestRequestSpec))
                .headers(getHttpHeadersConsumer(RestRequestSpec))
                .retrieve();
    }

    private WebClient.ResponseSpec patch(RestRequestSpec RestRequestSpec) {
        Assert.notNull(RestRequestSpec.getUrl(), "Request URL can not be null");
        Assert.notNull(RestRequestSpec.getBody(), "Request Body can not be null");
        return webClient
                .patch()
                .uri(RestRequestSpec.getUrl(), getUriVariables(RestRequestSpec))
                .bodyValue(RestRequestSpec.getBody())
                .headers(getHttpHeadersConsumer(RestRequestSpec))
                .retrieve();
    }

    private <R> Mono<R> toMono(Supplier<WebClient.ResponseSpec> supplier, Type toType) {
        return attachWebClientTP(supplier.get()
                .bodyToMono(ParameterizedTypeReference.forType(toType)));
    }

    private <R> Flux<R> toFlux(Supplier<WebClient.ResponseSpec> supplier, Type toType) {
        return attachWebClientTP(supplier.get()
                .bodyToFlux(ParameterizedTypeReference.forType(toType)));
    }

    private <R> Mono<R> attachWebClientTP(Mono<R> mono) {
        if (Objects.nonNull(webClientTaskExecutor)) {
            return mono.publishOn(Schedulers.fromExecutor(webClientTaskExecutor));
        }
        return mono;
    }

    private <R> Flux<R> attachWebClientTP(Flux<R> flux) {
        if (Objects.nonNull(webClientTaskExecutor)) {
            return flux.publishOn(Schedulers.fromExecutor(webClientTaskExecutor));
        }
        return flux;
    }

    private Map<String, String> getUriVariables(RestRequestSpec RestRequestSpec) {
        Map<String, String> uriVariables = new HashMap<>();
        if (Objects.nonNull(RestRequestSpec.getPathParams())) {
            uriVariables.putAll(RestRequestSpec.getPathParams());
        }
        if (Objects.nonNull(RestRequestSpec.getRequestParams())) {
            uriVariables.putAll(RestRequestSpec.getRequestParams());
        }
        return uriVariables;
    }

    private Consumer<HttpHeaders> getHttpHeadersConsumer(RestRequestSpec RestRequestSpec) {
        return (headers) -> {
            if (Objects.nonNull(RestRequestSpec.getHeaders())) {
                RestRequestSpec.getHeaders().forEach(headers::add);
            }
        };
    }
}
