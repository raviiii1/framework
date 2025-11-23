package com.ravi9a2.nca;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The RPCClientRegistry is a factory to create RPCClient instances which
 * stores all RPCClient instances in a registry.
 *
 * @author raviiii1
 */
public class RPCClientRegistry {

    static ConcurrentMap<String, RPCClient<?>> rpcClientRegistry = new ConcurrentHashMap<>();

    private <T> RPCClientRegistry(Map<String, RPCClient<T>> configs) {
        rpcClientRegistry.putAll(configs);
    }

    /**
     * Creates a RPCClientRegistry with a Map of shared RPCClient configurations.
     *
     * @param configs a Map of shared RPCClient configurations
     * @param <T>     Type of the Client that would be wrapped by a RPCClient
     * @return a RPCClientRegistry with a Map of shared RPCClient configurations.
     */
    public static <T> RPCClientRegistry of(Map<String, RPCClient<T>> configs) {
        return new RPCClientRegistry(configs);
    }

    /**
     * Returns a managed RPCClient or throws an IllegalArgumentException if not found.
     *
     * @param name the name of the RPCClient
     * @return The RPCClient
     */
    public RPCClient<?> client(String name) {
        if (Objects.isNull(rpcClientRegistry.get(name))) {
            throw new IllegalArgumentException(name);
        }
        return rpcClientRegistry.get(name);
    }

}