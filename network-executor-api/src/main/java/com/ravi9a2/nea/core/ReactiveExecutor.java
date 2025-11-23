package com.ravi9a2.nea.core;

import com.ravi9a2.nea.core.data.CallDefinition;
import com.ravi9a2.nca.ReactiveClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A generic interface that wraps a ReactiveClient and provides the APIs to execute a call
 * to a down-stream. It provides reactive APIs. The interface aims to extract the boilerplate
 * code for making a resilient call to a downstream via the Client.
 *
 * @param <C> The client.
 * @author raviprakash
 */
public interface ReactiveExecutor<C> extends Executor<ReactiveClient<C>> {

    /**
     * Reactive executor API that returns a Mono<R>.
     *
     * @param client  The Client
     * @param callDef The call definition
     * @param <D>     Call definition type
     * @param <R>     Response type
     * @return Mono of R
     */
    <D extends CallDefinition, R> Mono<R> executeToMono(ReactiveClient<C> client, D callDef);

    /**
     * Reactive executor API that returns a Flux<R>.
     *
     * @param client  The Client
     * @param callDef The call definition
     * @param <D>     Call definition type
     * @param <R>     Response type
     * @return Flux of R
     */
    <D extends CallDefinition, R> Flux<R> executeToFlux(ReactiveClient<C> client, D callDef);

}