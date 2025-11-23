package com.ravi9a2.nca;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The ReactiveClientRegistry is a factory to create ReactiveClient instances which
 * stores all ReactiveClient instances in a registry.
 *
 * @author raviiii1
 */
public class ReactiveClientRegistry {

    static ConcurrentMap<String, ReactiveClient<?>> reactiveClientRegistry = new ConcurrentHashMap<>();

    private <T> ReactiveClientRegistry(Map<String, ReactiveClient<T>> configs) {
        reactiveClientRegistry.putAll(configs);
    }

    /**
     * Creates a ReactiveClientRegistry with a Map of shared ReactiveClient configurations.
     *
     * @param configs a Map of shared ReactiveClient configurations
     * @param <T>     Type of the Client that would be wrapped by a ReactiveClient
     * @return a ReactiveClientRegistry with a Map of shared ReactiveClient configurations.
     */
    public static <T> ReactiveClientRegistry of(Map<String, ReactiveClient<T>> configs) {
        return new ReactiveClientRegistry(configs);
    }

    /**
     * Returns a managed ReactiveClient or throws an IllegalArgumentException if not found.
     *
     * @param name the name of the ReactiveClient
     * @return The ReactiveClient
     */
    public ReactiveClient<?> client(String name) {
        if (Objects.isNull(reactiveClientRegistry.get(name))) {
            throw new IllegalArgumentException(name);
        }
        return reactiveClientRegistry.get(name);
    }

}