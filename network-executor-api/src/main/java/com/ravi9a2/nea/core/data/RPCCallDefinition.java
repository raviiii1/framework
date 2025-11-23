package com.ravi9a2.nea.core.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A pojo extending CallDefinition to hold RPC specific call definition.
 *
 * @author raviprakash
 */
public class RPCCallDefinition extends CallDefinition {

    private String fqPackageName;
    private String className;
    private String methodName;
    private RPCMethod rpcMethod;
    private Map<String, String> grpcHeaders;

    public static Builder builder() {
        return new Builder();
    }

    protected RPCCallDefinition(Builder builder) {
        super(builder);
        this.fqPackageName = builder.fqPackageName;
        this.className = builder.className;
        this.grpcHeaders = builder.grpcHeaders;
        this.methodName = builder.methodName;
        this.rpcMethod = builder.rpcMethod;
    }

    public String getFqPackageName() {
        return fqPackageName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public RPCMethod getRpcMethod() {
        return rpcMethod;
    }

    public Map<String, String> getGrpcHeaders() {
        return grpcHeaders;
    }

    public static class Builder extends CallDefinition.Builder<Builder> {
        
        private String fqPackageName;
        private String className;
        private String methodName;
        private RPCMethod rpcMethod;
        private Map<String, String> grpcHeaders;

        public RPCCallDefinition.Builder fqPackageName(String fqPackageName) {
            this.fqPackageName = fqPackageName;
            return this;
        }

        public RPCCallDefinition.Builder rpcMethod(RPCMethod rpcMethod) {
            this.rpcMethod = rpcMethod;
            return this;
        }

        public RPCCallDefinition.Builder className(String className) {
            this.className = className;
            return this;
        }

        public RPCCallDefinition.Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public RPCCallDefinition.Builder grpcHeaders(Map<String, String> grpcHeaders) {
            this.grpcHeaders = mergeMap(this.grpcHeaders, grpcHeaders);
            return this;
        }


        @Override
        public RPCCallDefinition build() {
            return new RPCCallDefinition(this);
        }

        @Override
        protected RPCCallDefinition.Builder getThis() {
            return this;
        }

        private Map<String, String> mergeMap(Map<String, String> original, Map<String, String> map) {
            if (Objects.nonNull(map)) {
                if (Objects.isNull(original)) {
                    original = new HashMap<>();
                }
                original.putAll(map);
            }
            return original;
        }
    }
    
}
