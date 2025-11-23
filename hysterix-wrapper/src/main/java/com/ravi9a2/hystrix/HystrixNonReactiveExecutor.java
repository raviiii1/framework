package com.ravi9a2.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.ravi9a2.hystrix.config.HystrixCommandPropertiesRegistry;
import com.ravi9a2.hystrix.config.HystrixThreadPoolPropertiesRegistry;
import com.ravi9a2.nea.core.NonReactiveExecutor;
import com.ravi9a2.nea.core.data.CallDefinition;
import com.ravi9a2.nea.core.data.RestCallDefinition;
import com.ravi9a2.nca.NonReactiveClient;
import com.ravi9a2.nca.data.RestRequestSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Implements the NonReactiveExecutor interface using Netflix Hystrix.
 * Provides circuit breaker and thread pool isolation for blocking calls.
 *
 * @param <C> The client type
 * @author raviprakash
 */
@Service
public class HystrixNonReactiveExecutor<C> implements NonReactiveExecutor<C> {

    private static final Logger logger = LoggerFactory.getLogger(HystrixNonReactiveExecutor.class);

    private final HystrixCommandPropertiesRegistry commandPropertiesRegistry;
    private final HystrixThreadPoolPropertiesRegistry threadPoolPropertiesRegistry;

    @Autowired
    public HystrixNonReactiveExecutor(
            HystrixCommandPropertiesRegistry commandPropertiesRegistry,
            HystrixThreadPoolPropertiesRegistry threadPoolPropertiesRegistry) {
        this.commandPropertiesRegistry = commandPropertiesRegistry;
        this.threadPoolPropertiesRegistry = threadPoolPropertiesRegistry;
    }

    @Override
    public <D extends CallDefinition, R> R execute(NonReactiveClient<C> client, D callDef) {
        RestCallDefinition cd = (RestCallDefinition) callDef;
        return makeCall(client, cd);
    }

    @Override
    public <D extends CallDefinition, R> CompletableFuture<R> executeWithCompletionStage(NonReactiveClient<C> client,
            D callDef) {
        RestCallDefinition cd = (RestCallDefinition) callDef;
        return makeCallAsync(client, cd);
    }

    private <R> R makeCall(NonReactiveClient<C> client, RestCallDefinition cd) {
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
                return client.call(getRequestSpec(cd));
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

    private <R> CompletableFuture<R> makeCallAsync(NonReactiveClient<C> client, RestCallDefinition cd) {
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
                return client.call(getRequestSpec(cd));
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

        Future<R> future = command.queue();
        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
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
