package com.ravi9a2.nea.core;

import com.google.common.util.concurrent.ListenableFuture;
import com.ravi9a2.nea.core.data.CallDefinition;
import com.ravi9a2.nca.RPCClient;
import com.ravi9a2.nca.ReactiveClient;

public interface RPCExecutor<C> extends Executor<ReactiveClient<C>> {

    <D extends CallDefinition, R> R execute(RPCClient<C> client, D callDef);

    <D extends CallDefinition, R> ListenableFuture<R> executeAsync(RPCClient<C> client, D callDef);

}
