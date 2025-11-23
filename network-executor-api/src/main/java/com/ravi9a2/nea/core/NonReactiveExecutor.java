package com.ravi9a2.nea.core;

import com.ravi9a2.nea.core.data.CallDefinition;
import com.ravi9a2.nca.NonReactiveClient;

import java.util.concurrent.CompletableFuture;

/**
 * A generic interface that wraps a NonReactiveClient and provides the APIs to execute a call to
 * a down-stream. It provides no-reactive APIs. The interface aims to extract the boilerplate code
 * for making a resilient call to a downstream via the Client.
 *
 * @param <C> The client.
 * @author raviprakash
 */
public interface NonReactiveExecutor<C> extends Executor<NonReactiveClient<C>> {

    /**
     * Non-Reactive executor API.
     *
     * @param client  The Client
     * @param callDef The call definition
     * @param <D>     Call definition type
     * @param <R>     Response type
     * @return Response
     */
    <D extends CallDefinition, R> R execute(NonReactiveClient<C> client, D callDef);

    <D extends CallDefinition, R> CompletableFuture<R> executeWithCompletionStage(NonReactiveClient<C> var1, D var2);

}