package com.ravi9a2.nca;

import com.ravi9a2.nca.data.RestRequestSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The reactive client-wrapper that takes in a reactive client like Spring's Webflux etc.,
 * as generic parameter. It wraps the boilerplate setups for the clients and provides
 * standard APIs that enables segregation of the networking constructs from business logic.
 *
 * @param <C> A client type to wrap.
 * @author raviiii1
 */
public interface ReactiveClient<C> extends Client {

    /**
     * Registers a reactive HTTP Post call on the client to return a Mono of response `R`.
     *
     * @param <R> Response generic type.
     * @return A type of `Mono<R>`.
     */
    <R> Mono<R> postToMono(RestRequestSpec requestSpec);

    /**
     * Registers a reactive HTTP Put call on the client to return a Mono of response `R`.
     *
     * @param <R> Response generic type.
     * @return A type of `Mono<R>`.
     */
    <R> Mono<R> putToMono(RestRequestSpec requestSpec);

    /**
     * Registers a reactive HTTP Get call on the client to return a Mono of response `R`.
     *
     * @param <R> Response generic type.
     * @return A type of `Mono<R>`.
     */
    <R> Mono<R> getToMono(RestRequestSpec requestSpec);

    /**
     * Registers a reactive HTTP Delete call on the client to return a Mono of response `R`.
     *
     * @param <R> Response generic type.
     * @return A type of `Mono<R>`.
     */
    <R> Mono<R> deleteToMono(RestRequestSpec requestSpec);

    /**
     * Registers a reactive HTTP Options call on the client to return a Mono of response `R`.
     *
     * @param <R> Response generic type.
     * @return A type of `Mono<R>`.
     */
    <R> Mono<R> optionsToMono(RestRequestSpec requestSpec);

    /**
     * Registers a reactive HTTP Patch call on the client to return a Mono of response `R`.
     *
     * @param <R> Response generic type.
     * @return A type of `Mono<R>`.
     */
    <R> Mono<R> patchToMono(RestRequestSpec requestSpec);

    /**
     * Registers a reactive HTTP Post call on the client to return a Flux of response `R`.
     *
     * @param <R> Response generic type.
     * @return A type of `Flux<R>`.
     */
    <R> Flux<R> postToFlux(RestRequestSpec requestSpec);

    /**
     * Registers a reactive HTTP Put call on the client to return a Flux of response `R`.
     *
     * @param <R> Response generic type.
     * @return A type of `Flux<R>`.
     */
    <R> Flux<R> putToFlux(RestRequestSpec requestSpec);

    /**
     * Registers a reactive HTTP Get call on the client to return a Flux of response `R`.
     *
     * @param <R> Response generic type.
     * @return A type of `Flux<R>`.
     */
    <R> Flux<R> getToFlux(RestRequestSpec requestSpec);

    /**
     * Registers a reactive HTTP Delete call on the client to return a Flux of response `R`.
     *
     * @param <R> Response generic type.
     * @return A type of `Flux<R>`.
     */
    <R> Flux<R> deleteToFlux(RestRequestSpec requestSpec);

    /**
     * Registers a reactive HTTP Options call on the client to return a Flux of response `R`.
     *
     * @param <R> Response generic type.
     * @return A type of `Flux<R>`.
     */
    <R> Flux<R> optionsToFlux(RestRequestSpec requestSpec);

    /**
     * Registers a reactive HTTP Patch call on the client to return a Flux of response `R`.
     *
     * @param <R> Response generic type.
     * @return A type of `Flux<R>`.
     */
    <R> Flux<R> patchToFlux(RestRequestSpec requestSpec);

    /**
     * Registers a generic reactive call to return a Mono of response `R`
     *
     * @param <R> Response generic type.
     * @return A type of `Mono<R>`.
     */
    <R> Mono<R> callToMono(RestRequestSpec requestSpec);

    /**
     * Registers a generic reactive call to return a Flux of response `R`
     *
     * @param <R> Response generic type.
     * @return A type of `Flux<R>`.
     */
    <R> Flux<R> callToFlux(RestRequestSpec requestSpec);
}
