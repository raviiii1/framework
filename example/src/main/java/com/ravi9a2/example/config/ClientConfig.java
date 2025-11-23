package com.ravi9a2.example.config;

import com.ravi9a2.nca.RPCClientRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for client registries.
 * 
 * Note: NonReactiveClientRegistry is automatically created by httpclient-wrapper's
 * ClientConfigBeanLoader from application.properties.
 * 
 * ReactiveClientRegistry is automatically created by webclient-wrapper's
 * WebClientWrapperBeanLoader from application.properties.
 * 
 * This configuration provides RPCClientRegistry for gRPC services.
 * 
 * IMPORTANT: This is a placeholder implementation. A proper gRPC client wrapper
 * (GrpcClientWrapper) is required for production use. The RPCClientRegistry
 * is created here as an example, but actual gRPC client implementations
 * need to be provided.
 * 
 * @author raviprakash
 */
@Configuration
@Slf4j
public class ClientConfig {

    /**
     * Creates RPCClientRegistry for gRPC services.
     * 
     * Note: This is a placeholder. In production, you would:
     * 1. Create a grpc-wrapper library similar to httpclient-wrapper and webclient-wrapper
     * 2. Implement GrpcClientWrapper that wraps gRPC clients
     * 3. Create RPCClient instances from configuration
     * 4. Register them in the RPCClientRegistry
     * 
     * For now, this returns an empty registry to prevent startup errors.
     * The generated code will fail at runtime if gRPC services are called
     * without a proper implementation.
     */
    @Bean
    public RPCClientRegistry rpcClientRegistry() {
        log.warn("RPCClientRegistry created with empty configuration. " +
                "gRPC calls will fail without a proper GrpcClientWrapper implementation.");
        
        // Placeholder: Empty registry
        // In production, create RPCClient instances from configuration
        Map<String, com.ravi9a2.nca.RPCClient<?>> rpcClients = new HashMap<>();
        
        // Example of what would be done with a proper implementation:
        // RPCClient<GrpcClientWrapper> orderServiceClient = createGrpcClient("order-service", ...);
        // rpcClients.put("order-service", orderServiceClient);
        
        return RPCClientRegistry.of(rpcClients);
    }
}

