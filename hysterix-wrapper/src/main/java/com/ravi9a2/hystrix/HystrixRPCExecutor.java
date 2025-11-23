package com.ravi9a2.hystrix;

import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.ravi9a2.hystrix.config.HystrixCommandPropertiesRegistry;
import com.ravi9a2.hystrix.config.HystrixThreadPoolPropertiesRegistry;
import com.ravi9a2.nea.core.RPCExecutor;
import com.ravi9a2.nea.core.data.CallDefinition;
import com.ravi9a2.nea.core.data.RPCCallDefinition;
import com.ravi9a2.nca.RPCClient;
import com.ravi9a2.nca.data.RpcRequestSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

/**
 * Implements the RPCExecutor interface using Netflix Hystrix.
 * Provides circuit breaker and thread pool isolation for gRPC calls.
 *
 * @param <C> The client type
 * @author raviprakash
 */
@Service
public class HystrixRPCExecutor<C> implements RPCExecutor<C> {

    private static final Logger logger = LoggerFactory.getLogger(HystrixRPCExecutor.class);

    private final HystrixCommandPropertiesRegistry commandPropertiesRegistry;
    private final HystrixThreadPoolPropertiesRegistry threadPoolPropertiesRegistry;

    @Autowired
    public HystrixRPCExecutor(
            HystrixCommandPropertiesRegistry commandPropertiesRegistry,
            HystrixThreadPoolPropertiesRegistry threadPoolPropertiesRegistry) {
        this.commandPropertiesRegistry = commandPropertiesRegistry;
        this.threadPoolPropertiesRegistry = threadPoolPropertiesRegistry;
    }

    @Override
    public <D extends CallDefinition, R> R execute(RPCClient<C> client, D callDef) {
        RPCCallDefinition cd = (RPCCallDefinition) callDef;
        return makeCall(client, cd);
    }

    @Override
    public <D extends CallDefinition, R> ListenableFuture<R> executeAsync(RPCClient<C> client, D callDef) {
        RPCCallDefinition cd = (RPCCallDefinition) callDef;
        return makeCallAsync(client, cd);
    }

    private <R> R makeCall(RPCClient<C> client, RPCCallDefinition cd) {
        HystrixCommandProperties.Setter commandProperties = cd.isCircuitBreakerEnabled()
                ? commandPropertiesRegistry.getCommandProperties(cd.getCbTag())
                : commandPropertiesRegistry.getDefaultCommandProperties();

        HystrixThreadPoolProperties.Setter threadPoolProperties = cd.isBulkheadEnabled()
                ? threadPoolPropertiesRegistry.getThreadPoolProperties(cd.getBhTag())
                : threadPoolPropertiesRegistry.getDefaultThreadPoolProperties();

        HystrixCommand<R> command = new HystrixCommand<R>(
                HystrixCommand.Setter
                        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(cd.getServiceTag()))
                        .andCommandKey(HystrixCommandKey.Factory.asKey(cd.getCbTag()))
                        .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(cd.getBhTag()))
                        .andCommandPropertiesDefaults(commandProperties)
                        .andThreadPoolPropertiesDefaults(threadPoolProperties)) {

            @Override
            protected R run() throws Exception {
                return client.blocking(getRequestSpec(cd));
            }

            @Override
            protected R getFallback() {
                logger.warn("Hystrix fallback triggered for service: {}", cd.getServiceTag());
                if (cd.isSilentFailure()) {
                    return null;
                }
                throw new RuntimeException("Hystrix circuit breaker open or execution failed");
            }
        };

        return command.execute();
    }

    private <R> ListenableFuture<R> makeCallAsync(RPCClient<C> client, RPCCallDefinition cd) {
        HystrixCommandProperties.Setter commandProperties = cd.isCircuitBreakerEnabled()
                ? commandPropertiesRegistry.getCommandProperties(cd.getCbTag())
                : commandPropertiesRegistry.getDefaultCommandProperties();

        HystrixThreadPoolProperties.Setter threadPoolProperties = cd.isBulkheadEnabled()
                ? threadPoolPropertiesRegistry.getThreadPoolProperties(cd.getBhTag())
                : threadPoolPropertiesRegistry.getDefaultThreadPoolProperties();

        // For async calls, we wrap the client's ListenableFuture in Hystrix
        ListenableFuture<R> clientFuture = client.futureUnary(getRequestSpec(cd));

        // Create a Hystrix command that wraps the async call
        HystrixCommand<R> command = new HystrixCommand<R>(
                HystrixCommand.Setter
                        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(cd.getServiceTag()))
                        .andCommandKey(HystrixCommandKey.Factory.asKey(cd.getCbTag()))
                        .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(cd.getBhTag()))
                        .andCommandPropertiesDefaults(commandProperties)
                        .andThreadPoolPropertiesDefaults(threadPoolProperties)) {

            @Override
            protected R run() throws Exception {
                return clientFuture.get();
            }

            @Override
            protected R getFallback() {
                logger.warn("Hystrix fallback triggered for service: {}", cd.getServiceTag());
                if (cd.isSilentFailure()) {
                    return null;
                }
                throw new RuntimeException("Hystrix circuit breaker open or execution failed");
            }
        };

        Future<R> hystrixFuture = command.queue();
        // Convert Future to ListenableFuture using Guava
        return JdkFutureAdapters.listenInPoolThread(hystrixFuture);
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
