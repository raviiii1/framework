package com.ravi9a2.r4j;

import com.ravi9a2.nea.core.ReactiveExecutor;
import com.ravi9a2.nea.core.data.CallDefinition;
import com.ravi9a2.nea.core.data.RestCallDefinition;
import com.ravi9a2.nca.ReactiveClient;
import com.ravi9a2.nca.data.RestRequestSpec;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implements the `Executor<ReactiveClient<C>>` interface with resilience
 * solutions provided by R4J.
 *
 * @param <C>
 * @author raviprakash
 */
@Service
public class R4JReactiveExecutor<C> implements ReactiveExecutor<C> {

    private static final Logger logger = LoggerFactory.getLogger(R4JReactiveExecutor.class);

    CircuitBreakerRegistry circuitBreakerRegistry;
    BulkheadRegistry semaphoreBulkheadRegistry;

    @Autowired
    public R4JReactiveExecutor(CircuitBreakerRegistry circuitBreakerRegistry, BulkheadRegistry semaphoreBulkheadRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.semaphoreBulkheadRegistry = semaphoreBulkheadRegistry;
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
        Mono<R> res = client.callToMono(getRequestSpec(cd));
        if (cd.isBulkheadEnabled()) {
            res = res.transformDeferred(BulkheadOperator.of(semaphoreBulkheadRegistry.bulkhead(cd.getBhTag())));
        }
        if (cd.isCircuitBreakerEnabled()) {
            res = res.transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(cd.getCbTag())));
        }
        return res;
    }

    private <R> Flux<R> makeCallToFlux(ReactiveClient<C> client, RestCallDefinition cd) {
        Flux<R> res = client.callToFlux(getRequestSpec(cd));
        if (cd.isBulkheadEnabled()) {
            res = res.transformDeferred(BulkheadOperator.of(semaphoreBulkheadRegistry.bulkhead(cd.getBhTag())));
        }
        if (cd.isCircuitBreakerEnabled()) {
            res = res.transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(cd.getCbTag())));
        }
        return res;
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
