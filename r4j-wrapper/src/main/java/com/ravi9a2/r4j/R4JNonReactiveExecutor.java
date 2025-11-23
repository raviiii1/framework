package com.ravi9a2.r4j;

import com.ravi9a2.nea.core.NonReactiveExecutor;
import com.ravi9a2.nea.core.data.CallDefinition;
import com.ravi9a2.nea.core.data.RestCallDefinition;
import com.ravi9a2.nca.NonReactiveClient;
import com.ravi9a2.nca.data.RestRequestSpec;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * Implements the `Executor<ReactiveClient<C>>` interface with resilience
 * solutions provided by R4J.
 *
 * @param <C>
 * @author raviprakash
 */
@Service
public class R4JNonReactiveExecutor<C> implements NonReactiveExecutor<C> {

    private static final Logger logger = LoggerFactory.getLogger(R4JNonReactiveExecutor.class);

    CircuitBreakerRegistry circuitBreakerRegistry;
    ThreadPoolBulkheadRegistry tpBulkheadRegistry;
    BulkheadRegistry semaphoreBulkheadRegistry;

    @Autowired
    public R4JNonReactiveExecutor(CircuitBreakerRegistry circuitBreakerRegistry,
                                  ThreadPoolBulkheadRegistry tpBulkHeadRegistry,
                                  BulkheadRegistry semaphoreBulkheadRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.tpBulkheadRegistry = tpBulkHeadRegistry;
        this.semaphoreBulkheadRegistry = semaphoreBulkheadRegistry;
    }

    public <D extends CallDefinition, R> R execute(NonReactiveClient<C> client, D callDef) {
        RestCallDefinition cd = (RestCallDefinition) callDef;
        Supplier<R> supp = () -> client.call(getRequestSpec(cd));
        if (cd.isCircuitBreakerEnabled()) {
            supp = circuitBreakerRegistry
                    .circuitBreaker(cd.getCbTag())
                    .decorateSupplier(supp);
        }
        if (cd.isBulkheadEnabled()) {
            supp = Bulkhead.decorateSupplier(semaphoreBulkheadRegistry.bulkhead(cd.getBhTag()), supp);
        }
        return supp.get();
    }

    public <D extends CallDefinition, R> CompletableFuture<R> executeWithCompletionStage(NonReactiveClient<C> client, D callDef) {
        RestCallDefinition cd = (RestCallDefinition) callDef;
        Supplier<CompletionStage<R>> supp = ThreadPoolBulkhead.decorateSupplier(
                tpBulkheadRegistry.bulkhead(cd.getBhTag()),
                () -> client.call(getRequestSpec(cd))
        );
        if (cd.isCircuitBreakerEnabled()) {
            supp = circuitBreakerRegistry
                    .circuitBreaker(cd.getCbTag())
                    .decorateCompletionStage(supp);
        }
        return supp.get().toCompletableFuture();
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
