package com.ravi9a2.nca;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The NonReactiveClientRegistry is a factory to create NonReactiveClient instances which
 * stores all NonReactiveClient instances in a registry.
 *
 * @author raviiii1
 */
public class NonReactiveClientRegistry {

    static ConcurrentMap<String, NonReactiveClient<?>> nonReactiveClientRegistry = new ConcurrentHashMap<>();

    private <T> NonReactiveClientRegistry(Map<String, NonReactiveClient<T>> configs) {
        nonReactiveClientRegistry.putAll(configs);
    }

    /**
     * Creates a NonReactiveClientRegistry with a Map of shared NonReactiveClient configurations.
     *
     * @param configs a Map of shared NonReactiveClient configurations
     * @param <T>     Type of the Client that would be wrapped by a NonReactiveClient
     * @return a NonReactiveClientRegistry with a Map of shared NonReactiveClient configurations.
     */
    public static <T> NonReactiveClientRegistry of(Map<String, NonReactiveClient<T>> configs) {
        return new NonReactiveClientRegistry(configs);
    }

    /**
     * Returns a managed NonReactiveClient or throws an IllegalArgumentException if not found.
     *
     * @param name the name of the NonReactiveClient
     * @return The NonReactiveClient
     */
    public NonReactiveClient<?> client(String name) {
        if (Objects.isNull(nonReactiveClientRegistry.get(name))) {
            throw new IllegalArgumentException(name);
        }
        return nonReactiveClientRegistry.get(name);
    }
}