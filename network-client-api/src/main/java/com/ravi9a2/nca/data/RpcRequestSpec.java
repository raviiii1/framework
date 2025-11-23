package com.ravi9a2.nca.data;

public class RpcRequestSpec extends RequestSpec {

    protected String fqPackageName;
    protected String methodName;
    protected String serviceName;
    protected String rpcMethod;

    protected RpcRequestSpec(Builder builder) {
        super(builder);
        this.methodName = builder.methodName;
        this.serviceName = builder.serviceName;
        this.fqPackageName = builder.fqPackageName;
        this.rpcMethod = builder.rpcMethod;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getMethodName() {
        return methodName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getFqPackageName() {
        return fqPackageName;
    }

    public String getRpcMethod() {
        return rpcMethod;
    }

    public static class Builder extends RequestSpec.Builder<Builder> {
        private String methodName;
        private String serviceName;
        private String fqPackageName;
        private String rpcMethod;

        public Builder fqPackageName(String fqPackageName) {
            this.fqPackageName = fqPackageName;
            return this.getThis();
        }

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this.getThis();
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this.getThis();
        }

        public Builder rpcMethod(String rpcMethod) {
            this.rpcMethod = rpcMethod;
            return this.getThis();
        }

        @Override
        public RpcRequestSpec build() {
            return new RpcRequestSpec(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }

}
