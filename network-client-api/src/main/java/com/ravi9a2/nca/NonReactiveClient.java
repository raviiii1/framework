package com.ravi9a2.nca;

import com.ravi9a2.nca.data.RestRequestSpec;

/**
 * The non-reactive client-wrapper that takes in a non-reactive client like Spring's
 * RestTemplate or Apache's HTTPClient etc., as generic parameter. It wraps the
 * boilerplate setups for the clients and provides standard APIs that enables segregation
 * of the networking constructs from business logic.
 *
 * @param <C> A client type to wrap.
 * @author raviiii1
 */
public interface NonReactiveClient<C> extends Client {

    /**
     * Makes HTTP Post call on the client.
     *
     * @param <R> Response generic type.
     * @return A type of `R`.
     */
    <R> R post(RestRequestSpec requestSpec);

    /**
     * Makes HTTP Put call on the client.
     *
     * @param <R> Response generic type.
     * @return A type of `R`.
     */
    <R> R put(RestRequestSpec requestSpec);

    /**
     * Makes HTTP Get call on the client.
     *
     * @param <R> Response generic type.
     * @return A type of `R`.
     */
    <R> R get(RestRequestSpec requestSpec);

    /**
     * Makes HTTP Delete call on the client.
     *
     * @param <R> Response generic type.
     * @return A type of `R`.
     */
    <R> R delete(RestRequestSpec requestSpec);

    /**
     * Makes HTTP Options call on the client.
     *
     * @param <R> Response generic type.
     * @return A type of `R`.
     */
    <R> R options(RestRequestSpec requestSpec);

    /**
     * Makes HTTP Patch call on the client.
     *
     * @param <R> Response generic type.
     * @return A type of `R`.
     */
    <R> R patch(RestRequestSpec requestSpec);

    /**
     * Registers a generic non-reactive call to return response `R`
     *
     * @param <R> Response generic type.
     * @return A type of `Flux<R>`.
     */
    <R> R call(RestRequestSpec requestSpec);

}
