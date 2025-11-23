package com.ravi9a2.grpc;

import com.ravi9a2.nca.RPCClientRegistry;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Spring configuration for loading gRPC client wrappers.
 * Reads configuration from application.properties with prefix "downstream.rpc".
 * 
 * @author raviprakash
 */
@Slf4j
@Configuration
public class GrpcClientWrapperBeanLoader {

    private static final String DEFAULT = "default";

    @ConfigurationProperties(prefix = "downstream.rpc")
    public static class GrpcClientProperties {
        private Map<String, GrpcClientConfig> clients = new HashMap<>();

        public Map<String, GrpcClientConfig> getClients() {
            return clients;
        }

        public void setClients(Map<String, GrpcClientConfig> clients) {
            this.clients = clients;
        }
    }

    public static class GrpcClientConfig {
        private String target;
        private Long timeoutSeconds;
        private Boolean usePlaintext;

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public Long getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(Long timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public Boolean getUsePlaintext() {
            return usePlaintext;
        }

        public void setUsePlaintext(Boolean usePlaintext) {
            this.usePlaintext = usePlaintext;
        }
    }

    @Bean
    public RPCClientRegistry grpcClientRegistry(GrpcClientProperties properties) {
        Map<String, GrpcClientWrapper> clients = new HashMap<>();

        if (properties.getClients() != null && !properties.getClients().isEmpty()) {
            for (Map.Entry<String, GrpcClientConfig> entry : properties.getClients().entrySet()) {
                String serviceName = entry.getKey();
                GrpcClientConfig config = entry.getValue();

                if (config.getTarget() == null || config.getTarget().isEmpty()) {
                    log.warn("Skipping gRPC client '{}' - target not configured", serviceName);
                    continue;
                }

                GrpcClientWrapperBuilder builder = GrpcClientWrapperBuilder.builder()
                        .target(config.getTarget());

                if (config.getTimeoutSeconds() != null) {
                    builder.timeoutSeconds(config.getTimeoutSeconds());
                }

                if (config.getUsePlaintext() != null) {
                    builder.usePlaintext(config.getUsePlaintext());
                }

                GrpcClientWrapper wrapper = builder.build();
                clients.put(serviceName, wrapper);
                log.info("Registered gRPC client '{}' with target: {}", serviceName, config.getTarget());
            }
        }

        if (clients.isEmpty()) {
            log.warn("No gRPC clients configured. RPC calls will fail without proper configuration.");
        }

        // Convert GrpcClientWrapper map to RPCClient map
        @SuppressWarnings("unchecked")
        Map<String, com.ravi9a2.nca.RPCClient<io.grpc.ManagedChannel>> rpcClients = new HashMap<>();
        for (Map.Entry<String, GrpcClientWrapper> entry : clients.entrySet()) {
            rpcClients.put(entry.getKey(), entry.getValue());
        }

        return com.ravi9a2.nca.RPCClientRegistry.of(rpcClients);
    }

    @Bean
    public GrpcClientProperties grpcClientProperties() {
        return new GrpcClientProperties();
    }
}
