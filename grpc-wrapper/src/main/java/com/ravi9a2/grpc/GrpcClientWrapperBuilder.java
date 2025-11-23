package com.ravi9a2.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Builder for creating GrpcClientWrapper instances.
 * 
 * @author raviprakash
 */
@Slf4j
public class GrpcClientWrapperBuilder {

    private String target;
    private long timeoutSeconds = 30;
    private boolean usePlaintext = false;

    private GrpcClientWrapperBuilder() {
    }

    public static GrpcClientWrapperBuilder builder() {
        return new GrpcClientWrapperBuilder();
    }

    public GrpcClientWrapperBuilder target(String target) {
        this.target = target;
        return this;
    }

    public GrpcClientWrapperBuilder timeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public GrpcClientWrapperBuilder usePlaintext(boolean usePlaintext) {
        this.usePlaintext = usePlaintext;
        return this;
    }

    public GrpcClientWrapper build() {
        if (target == null || target.isEmpty()) {
            throw new IllegalArgumentException("Target cannot be null or empty");
        }

        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forTarget(target);

        if (usePlaintext) {
            channelBuilder.usePlaintext();
        }

        ManagedChannel channel = channelBuilder.build();
        log.info("Created gRPC channel for target: {}", target);

        return new GrpcClientWrapper(channel, timeoutSeconds);
    }
}
