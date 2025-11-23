package com.ravi9a2.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import com.netflix.hystrix.HystrixObservableCommand.Setter;
import com.ravi9a2.hystrix.config.HystrixCommandPropertiesRegistry;
import com.ravi9a2.nea.core.ReactiveExecutor;
import com.ravi9a2.nea.core.data.CallDefinition;
import com.ravi9a2.nea.core.data.RestCallDefinition;
import com.ravi9a2.nca.ReactiveClient;
import com.ravi9a2.nca.data.RestRequestSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;
import org.reactivestreams.Publisher;

/**
 * Implements the ReactiveExecutor interface using Netflix Hystrix.
 * Provides circuit breaker and thread pool isolation for reactive calls.
 *
 * @param <C> The client type
 * @author raviprakash
 */
@Service
public class HystrixReactiveExecutor<C> implements ReactiveExecutor<C> {

    private static final Logger logger = LoggerFactory.getLogger(HystrixReactiveExecutor.class);

    private final HystrixCommandPropertiesRegistry commandPropertiesRegistry;

    @Autowired
    public HystrixReactiveExecutor(HystrixCommandPropertiesRegistry commandPropertiesRegistry) {
        this.commandPropertiesRegistry = commandPropertiesRegistry;
    }

    @Override
    public <D extends CallDefinition, R> Mono<R> executeToMono(ReactiveClient<C> client, D callDef) {
        RestCallDefinition cd = (RestCallDefinition) callDef;
        return makeCallToMono(client, cd);
    }

    @Override
    public <D extends CallDefinition, R> Flux<R> executeToFlux(ReactiveClient<C> client, D callDef) {
        RestCallDefinition cd = (RestCallDefinition) callDef;
        return makeCallToFlux(client, cd);
    }

    private <R> Mono<R> makeCallToMono(ReactiveClient<C> client, RestCallDefinition cd) {
        HystrixCommandProperties.Setter commandProperties = cd.isCircuitBreakerEnabled() 
                ? commandPropertiesRegistry.getCommandProperties(cd.getCbTag())
                : commandPropertiesRegistry.getDefaultCommandProperties();
        
        HystrixObservableCommand<R> command = new HystrixObservableCommand<R>(
                Setter
                        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(cd.getServiceTag()))
                        .andCommandKey(HystrixCommandKey.Factory.asKey(cd.getCbTag()))
                        .andCommandPropertiesDefaults(commandProperties)) {

            @Override
            protected Observable<R> construct() {
                Mono<R> mono = client.callToMono(getRequestSpec(cd));
                Publisher<R> publisher = mono;
                return RxReactiveStreams.toObservable(publisher);
            }

            @Override
            protected Observable<R> resumeWithFallback() {
                logger.warn("Hystrix fallback triggered for service: {}", cd.getServiceTag());
                if (cd.isSilentFailure()) {
                    return Observable.empty();
                }
                return Observable.error(new RuntimeException("Hystrix circuit breaker open or execution failed"));
            }
        };

        Observable<R> observable = command.observe();
        return Mono.from(RxReactiveStreams.toPublisher(observable));
    }

    private <R> Flux<R> makeCallToFlux(ReactiveClient<C> client, RestCallDefinition cd) {
        HystrixCommandProperties.Setter commandProperties = cd.isCircuitBreakerEnabled() 
                ? commandPropertiesRegistry.getCommandProperties(cd.getCbTag())
                : commandPropertiesRegistry.getDefaultCommandProperties();
        
        HystrixObservableCommand<R> command = new HystrixObservableCommand<R>(
                Setter
                        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(cd.getServiceTag()))
                        .andCommandKey(HystrixCommandKey.Factory.asKey(cd.getCbTag()))
                        .andCommandPropertiesDefaults(commandProperties)) {

            @Override
            protected Observable<R> construct() {
                Flux<R> flux = client.callToFlux(getRequestSpec(cd));
                return RxReactiveStreams.toObservable(flux);
            }

            @Override
            protected Observable<R> resumeWithFallback() {
                logger.warn("Hystrix fallback triggered for service: {}", cd.getServiceTag());
                if (cd.isSilentFailure()) {
                    return Observable.empty();
                }
                return Observable.error(new RuntimeException("Hystrix circuit breaker open or execution failed"));
            }
        };

        Observable<R> observable = command.observe();
        return Flux.from(RxReactiveStreams.toPublisher(observable));
    }

    private RestRequestSpec getRequestSpec(RestCallDefinition cd) {
        return RestRequestSpec.builder()
                .httpMethod(String.valueOf(cd.getHttpMethod()))
                .url(cd.getPath())
                .body(cd.getPayload())
                .pathParams(cd.getPathParams())
                .requestParams(cd.getQueryParams())
                .headers(cd.getHttpHeaders())
                .type(cd.getResponseType())
                .build();
    }
}
