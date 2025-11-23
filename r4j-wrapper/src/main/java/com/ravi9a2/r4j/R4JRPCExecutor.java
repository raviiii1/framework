package com.ravi9a2.r4j;

import com.google.common.util.concurrent.ListenableFuture;
import com.ravi9a2.nea.core.RPCExecutor;
import com.ravi9a2.nea.core.data.CallDefinition;
import com.ravi9a2.nea.core.data.RPCCallDefinition;
import com.ravi9a2.nca.RPCClient;
import com.ravi9a2.nca.data.RpcRequestSpec;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;
import java.util.function.Supplier;

@Service
public class R4JRPCExecutor<C> implements RPCExecutor<C> {

    CircuitBreakerRegistry circuitBreakerRegistry;
    BulkheadRegistry semaphoreBulkheadRegistry;

    @Autowired
    public R4JRPCExecutor(CircuitBreakerRegistry circuitBreakerRegistry, BulkheadRegistry semaphoreBulkheadRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.semaphoreBulkheadRegistry = semaphoreBulkheadRegistry;
    }

    @Override
    public <D extends CallDefinition, R> R execute(RPCClient<C> client, D callDef) {
        RPCCallDefinition cd = (RPCCallDefinition) callDef;
        Supplier<R> supp = () -> client.blocking(getRequestSpec(cd));
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

    @Override
    public <D extends CallDefinition, R> ListenableFuture<R> executeAsync(RPCClient<C> client, D callDef) {
        RPCCallDefinition cd = (RPCCallDefinition) callDef;
        Supplier<Future<R>> supp = Bulkhead.decorateFuture(
                semaphoreBulkheadRegistry.bulkhead(cd.getBhTag()),
                () -> client.futureUnary(getRequestSpec(cd))
        );
        if (cd.isCircuitBreakerEnabled()) {
            supp = circuitBreakerRegistry
                    .circuitBreaker(cd.getCbTag())
                    .decorateFuture(supp);
        }
        return (ListenableFuture<R>) supp.get();
    }

    private RpcRequestSpec getRequestSpec(RPCCallDefinition cd) {
        return RpcRequestSpec.builder()
                .rpcMethod(cd.getRpcMethod().toString())
                .fqPackageName(cd.getFqPackageName())
                .methodName(cd.getMethodName())
                .serviceName(cd.getClassName())
                .body(cd.getPayload())
                .type(cd.getResponseType())
                .headers(cd.getGrpcHeaders())
                .build();
    }
}
