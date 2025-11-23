package com.ravi9a2.nca;

import com.google.common.util.concurrent.ListenableFuture;
import com.ravi9a2.nca.data.RpcRequestSpec;

public interface RPCClient<C> extends Client {

    <R> ListenableFuture<R> future(RpcRequestSpec requestSpec);

    <R> ListenableFuture<R> futureUnary(RpcRequestSpec requestSpec);

    <R> ListenableFuture<R> futureBiDi(RpcRequestSpec requestSpec);

    <R> ListenableFuture<R> futureServerStreaming(RpcRequestSpec requestSpec);

    <R> ListenableFuture<R> futureClientStreaming(RpcRequestSpec requestSpec);

    <R> R blocking(RpcRequestSpec requestSpec);

    <R> R blockingUnary(RpcRequestSpec requestSpec);

    <R> R blockingBiDi(RpcRequestSpec requestSpec);

    <R> R blockingServerStreaming(RpcRequestSpec requestSpec);

    <R> R blockingClientStreaming(RpcRequestSpec requestSpec);

}
