package com.ravi9a2.r4j.aspect;

import com.ravi9a2.nea.annotations.BidiRPCCall;
import com.ravi9a2.nea.annotations.Call;
import com.ravi9a2.nea.annotations.ClientStreamRPCCall;
import com.ravi9a2.nea.annotations.DeleteCall;
import com.ravi9a2.nea.annotations.GetCall;
import com.ravi9a2.nea.annotations.PatchCall;
import com.ravi9a2.nea.annotations.PostCall;
import com.ravi9a2.nea.annotations.PutCall;
import com.ravi9a2.nea.annotations.ServiceStreamRPCCall;
import com.ravi9a2.nea.annotations.UnaryRPCCall;
import com.ravi9a2.nea.core.data.HTTPMethod;
import com.ravi9a2.nea.core.data.RPCMethod;
import com.ravi9a2.nea.core.data.Type;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * A wrapper class to wrap all the call annotations. It provides
 * generic methods that work across the call annotations.
 *
 * @author raviprakash
 */
public class AnnotationWrapper {

    private PostCall postCall;
    private GetCall getCall;
    private Call call;
    private DeleteCall deleteCall;
    private PatchCall patchCall;
    private PutCall putCall;

    private BidiRPCCall bidiRPCCall;

    private ClientStreamRPCCall clientStreamRPCCall;

    private ServiceStreamRPCCall serviceStreamRPCCall;

    private UnaryRPCCall unaryRPCCall;

    private AnnotationWrapper(PostCall postCall) {
        this.postCall = postCall;
    }

    private AnnotationWrapper(GetCall getCall) {
        this.getCall = getCall;
    }

    private AnnotationWrapper(Call call) {
        this.call = call;
    }

    private AnnotationWrapper(DeleteCall call) {
        this.deleteCall = call;
    }

    private AnnotationWrapper(PatchCall call) {
        this.patchCall = call;
    }

    private AnnotationWrapper(PutCall call) {
        this.putCall = call;
    }

    private AnnotationWrapper(BidiRPCCall call) {
        this.bidiRPCCall = call;
    }

    private AnnotationWrapper(ClientStreamRPCCall call) {
        this.clientStreamRPCCall = call;
    }

    private AnnotationWrapper(ServiceStreamRPCCall call) {
        this.serviceStreamRPCCall = call;
    }

    private AnnotationWrapper(UnaryRPCCall call) {
        this.unaryRPCCall = call;
    }

    public static AnnotationWrapper wrap(PostCall postCall) {
        if (Objects.isNull(postCall)) {
            return null;
        }
        return new AnnotationWrapper(postCall);
    }

    public static AnnotationWrapper wrap(GetCall getCall) {
        if (Objects.isNull(getCall)) {
            return null;
        }
        return new AnnotationWrapper(getCall);
    }

    public static AnnotationWrapper wrap(Call call) {
        if (Objects.isNull(call)) {
            return null;
        }
        return new AnnotationWrapper(call);
    }

    public static AnnotationWrapper wrap(DeleteCall deleteCall) {
        if (Objects.isNull(deleteCall)) {
            return null;
        }
        return new AnnotationWrapper(deleteCall);
    }

    public static AnnotationWrapper wrap(PatchCall patchCall) {
        if (Objects.isNull(patchCall)) {
            return null;
        }
        return new AnnotationWrapper(patchCall);
    }

    public static AnnotationWrapper wrap(PutCall postCall) {
        if (Objects.isNull(postCall)) {
            return null;
        }
        return new AnnotationWrapper(postCall);
    }

    public static AnnotationWrapper wrap(BidiRPCCall bidiRPCCall) {
        if (Objects.isNull(bidiRPCCall)) {
            return null;
        }
        return new AnnotationWrapper(bidiRPCCall);
    }

    public static AnnotationWrapper wrap(ClientStreamRPCCall clientStreamRPCCall) {
        if (Objects.isNull(clientStreamRPCCall)) {
            return null;
        }
        return new AnnotationWrapper(clientStreamRPCCall);
    }

    public static AnnotationWrapper wrap(ServiceStreamRPCCall serviceStreamRPCCall) {
        if (Objects.isNull(serviceStreamRPCCall)) {
            return null;
        }
        return new AnnotationWrapper(serviceStreamRPCCall);
    }

    public static AnnotationWrapper wrap(UnaryRPCCall unaryRPCCall) {
        if (Objects.isNull(unaryRPCCall)) {
            return null;
        }
        return new AnnotationWrapper(unaryRPCCall);
    }

    public String path() {
        if (Objects.nonNull(call)) {
            return call.path();
        } else if (Objects.nonNull(postCall)) {
            return postCall.path();
        } else if (Objects.nonNull(getCall)) {
            return getCall.path();
        } else if (Objects.nonNull(patchCall)) {
            return patchCall.path();
        } else if (Objects.nonNull(deleteCall)) {
            return deleteCall.path();
        } else if (Objects.nonNull(putCall)) {
            return putCall.path();
        }
        return "";
    }

    public String service() {
        if (Objects.nonNull(call)) {
            return call.service();
        } else if (Objects.nonNull(postCall)) {
            return postCall.service();
        } else if (Objects.nonNull(getCall)) {
            return getCall.service();
        } else if (Objects.nonNull(patchCall)) {
            return patchCall.service();
        } else if (Objects.nonNull(deleteCall)) {
            return deleteCall.service();
        } else if (Objects.nonNull(putCall)) {
            return putCall.service();
        } else if (Objects.nonNull(unaryRPCCall)) {
            return unaryRPCCall.service();
        } else if (Objects.nonNull(bidiRPCCall)) {
            return bidiRPCCall.service();
        } else if (Objects.nonNull(serviceStreamRPCCall)) {
            return serviceStreamRPCCall.service();
        } else if (Objects.nonNull(clientStreamRPCCall)) {
            return clientStreamRPCCall.service();
        }
        return "";
    }

    public boolean isSilent() {
        if (Objects.nonNull(call)) {
            return call.isSilent();
        } else if (Objects.nonNull(postCall)) {
            return postCall.isSilent();
        } else if (Objects.nonNull(getCall)) {
            return getCall.isSilent();
        } else if (Objects.nonNull(patchCall)) {
            return patchCall.isSilent();
        } else if (Objects.nonNull(deleteCall)) {
            return deleteCall.isSilent();
        } else if (Objects.nonNull(putCall)) {
            return putCall.isSilent();
        } else if (Objects.nonNull(unaryRPCCall)) {
            return unaryRPCCall.isSilent();
        } else if (Objects.nonNull(bidiRPCCall)) {
            return bidiRPCCall.isSilent();
        } else if (Objects.nonNull(serviceStreamRPCCall)) {
            return serviceStreamRPCCall.isSilent();
        } else if (Objects.nonNull(clientStreamRPCCall)) {
            return clientStreamRPCCall.isSilent();
        }
        return true;
    }

    public boolean isRetryable() {
        if (Objects.nonNull(call)) {
            return call.isRetryable();
        } else if (Objects.nonNull(postCall)) {
            return postCall.isRetryable();
        } else if (Objects.nonNull(getCall)) {
            return getCall.isRetryable();
        } else if (Objects.nonNull(patchCall)) {
            return patchCall.isRetryable();
        } else if (Objects.nonNull(deleteCall)) {
            return deleteCall.isRetryable();
        } else if (Objects.nonNull(putCall)) {
            return putCall.isRetryable();
        } else if (Objects.nonNull(unaryRPCCall)) {
            return unaryRPCCall.isRetryable();
        } else if (Objects.nonNull(bidiRPCCall)) {
            return bidiRPCCall.isRetryable();
        } else if (Objects.nonNull(serviceStreamRPCCall)) {
            return serviceStreamRPCCall.isRetryable();
        } else if (Objects.nonNull(clientStreamRPCCall)) {
            return clientStreamRPCCall.isRetryable();
        }
        return false;
    }

    public boolean cbEnabled() {
        if (Objects.nonNull(call)) {
            return call.cbEnabled();
        } else if (Objects.nonNull(postCall)) {
            return postCall.cbEnabled();
        } else if (Objects.nonNull(getCall)) {
            return getCall.cbEnabled();
        } else if (Objects.nonNull(patchCall)) {
            return patchCall.cbEnabled();
        } else if (Objects.nonNull(deleteCall)) {
            return deleteCall.cbEnabled();
        } else if (Objects.nonNull(putCall)) {
            return putCall.cbEnabled();
        } else if (Objects.nonNull(unaryRPCCall)) {
            return unaryRPCCall.cbEnabled();
        } else if (Objects.nonNull(bidiRPCCall)) {
            return bidiRPCCall.cbEnabled();
        } else if (Objects.nonNull(serviceStreamRPCCall)) {
            return serviceStreamRPCCall.cbEnabled();
        } else if (Objects.nonNull(clientStreamRPCCall)) {
            return clientStreamRPCCall.cbEnabled();
        }
        return false;
    }

    public boolean bhEnabled() {
        if (Objects.nonNull(call)) {
            return call.bhEnabled();
        } else if (Objects.nonNull(postCall)) {
            return postCall.bhEnabled();
        } else if (Objects.nonNull(getCall)) {
            return getCall.bhEnabled();
        } else if (Objects.nonNull(patchCall)) {
            return patchCall.bhEnabled();
        } else if (Objects.nonNull(deleteCall)) {
            return deleteCall.bhEnabled();
        } else if (Objects.nonNull(putCall)) {
            return putCall.bhEnabled();
        } else if (Objects.nonNull(unaryRPCCall)) {
            return unaryRPCCall.bhEnabled();
        } else if (Objects.nonNull(bidiRPCCall)) {
            return bidiRPCCall.bhEnabled();
        } else if (Objects.nonNull(serviceStreamRPCCall)) {
            return serviceStreamRPCCall.bhEnabled();
        } else if (Objects.nonNull(clientStreamRPCCall)) {
            return clientStreamRPCCall.bhEnabled();
        }
        return false;
    }

    public HTTPMethod method() {
        if (Objects.nonNull(call)) {
            return call.method();
        } else if (Objects.nonNull(postCall)) {
            return HTTPMethod.POST;
        } else if (Objects.nonNull(getCall)) {
            return HTTPMethod.GET;
        } else if (Objects.nonNull(deleteCall)) {
            return HTTPMethod.DELETE;
        } else if (Objects.nonNull(patchCall)) {
            return HTTPMethod.PATCH;
        } else if (Objects.nonNull(putCall)) {
            return HTTPMethod.PUT;
        }
        return HTTPMethod.POST;
    }

    public RPCMethod rpcMethod() {
        if (Objects.nonNull(unaryRPCCall)) {
            return unaryRPCCall.method();
        } else if (Objects.nonNull(bidiRPCCall)) {
            return bidiRPCCall.method();
        } else if (Objects.nonNull(serviceStreamRPCCall)) {
            return serviceStreamRPCCall.method();
        } else if (Objects.nonNull(clientStreamRPCCall)) {
            return clientStreamRPCCall.method();
        }
        return RPCMethod.UNARY;
    }

    public String fqPackageName() {
        if (Objects.nonNull(unaryRPCCall)) {
            return unaryRPCCall.fqPackageName();
        } else if (Objects.nonNull(bidiRPCCall)) {
            return bidiRPCCall.fqPackageName();
        } else if (Objects.nonNull(serviceStreamRPCCall)) {
            return serviceStreamRPCCall.fqPackageName();
        } else if (Objects.nonNull(clientStreamRPCCall)) {
            return clientStreamRPCCall.fqPackageName();
        }
        return "";
    }

    public String className() {
        if (Objects.nonNull(unaryRPCCall)) {
            return unaryRPCCall.className();
        } else if (Objects.nonNull(bidiRPCCall)) {
            return bidiRPCCall.className();
        } else if (Objects.nonNull(serviceStreamRPCCall)) {
            return serviceStreamRPCCall.className();
        } else if (Objects.nonNull(clientStreamRPCCall)) {
            return clientStreamRPCCall.className();
        }
        return "";
    }

    public String methodName() {
        if (Objects.nonNull(unaryRPCCall)) {
            return unaryRPCCall.methodName();
        } else if (Objects.nonNull(bidiRPCCall)) {
            return bidiRPCCall.methodName();
        } else if (Objects.nonNull(serviceStreamRPCCall)) {
            return serviceStreamRPCCall.methodName();
        } else if (Objects.nonNull(clientStreamRPCCall)) {
            return clientStreamRPCCall.methodName();
        }
        return "";
    }

    public Type type() {
        if (Objects.nonNull(call)) {
            return call.type();
        } else if (Objects.nonNull(postCall) || Objects.nonNull(getCall) || Objects.nonNull(patchCall) ||
                Objects.nonNull(deleteCall) || Objects.nonNull(putCall)) {
            return Type.HTTP;
        } else if (Objects.nonNull(bidiRPCCall) || Objects.nonNull(clientStreamRPCCall) ||
                Objects.nonNull(serviceStreamRPCCall) || Objects.nonNull(unaryRPCCall)) {
            return Type.RPC;
        }
        return Type.HTTP;
    }


    public String bulkhead() {
        if (Objects.nonNull(call)) {
            return getOrDefault(call.bulkhead(), call.service());
        } else if (Objects.nonNull(postCall)) {
            return getOrDefault(postCall.bulkhead(), postCall.service());
        } else if (Objects.nonNull(getCall)) {
            return getOrDefault(getCall.bulkhead(), getCall.service());
        } else if (Objects.nonNull(patchCall)) {
            return getOrDefault(patchCall.bulkhead(), patchCall.service());
        } else if (Objects.nonNull(deleteCall)) {
            return getOrDefault(deleteCall.bulkhead(), deleteCall.service());
        } else if (Objects.nonNull(putCall)) {
            return getOrDefault(putCall.bulkhead(), putCall.service());
        } else if (Objects.nonNull(unaryRPCCall)) {
            return getOrDefault(unaryRPCCall.bulkhead(), unaryRPCCall.service());
        } else if (Objects.nonNull(bidiRPCCall)) {
            return getOrDefault(bidiRPCCall.bulkhead(), bidiRPCCall.service());
        } else if (Objects.nonNull(serviceStreamRPCCall)) {
            return getOrDefault(serviceStreamRPCCall.bulkhead(), serviceStreamRPCCall.service());
        } else if (Objects.nonNull(clientStreamRPCCall)) {
            return getOrDefault(clientStreamRPCCall.bulkhead(), clientStreamRPCCall.service());
        }
        return "";
    }

    public String circuitBreaker() {
        if (Objects.nonNull(call)) {
            return getOrDefault(call.circuitBreaker(), call.service());
        } else if (Objects.nonNull(postCall)) {
            return getOrDefault(postCall.circuitBreaker(), postCall.service());
        } else if (Objects.nonNull(getCall)) {
            return getOrDefault(getCall.circuitBreaker(), getCall.service());
        } else if (Objects.nonNull(patchCall)) {
            return getOrDefault(patchCall.circuitBreaker(), patchCall.service());
        } else if (Objects.nonNull(deleteCall)) {
            return getOrDefault(deleteCall.circuitBreaker(), deleteCall.service());
        } else if (Objects.nonNull(putCall)) {
            return getOrDefault(putCall.circuitBreaker(), putCall.service());
        } else if (Objects.nonNull(unaryRPCCall)) {
            return getOrDefault(unaryRPCCall.circuitBreaker(), unaryRPCCall.service());
        } else if (Objects.nonNull(bidiRPCCall)) {
            return getOrDefault(bidiRPCCall.circuitBreaker(), bidiRPCCall.service());
        } else if (Objects.nonNull(serviceStreamRPCCall)) {
            return getOrDefault(serviceStreamRPCCall.circuitBreaker(), serviceStreamRPCCall.service());
        } else if (Objects.nonNull(clientStreamRPCCall)) {
            return getOrDefault(clientStreamRPCCall.circuitBreaker(), clientStreamRPCCall.service());
        }
        return "";
    }

    public String retry() {
        if (Objects.nonNull(call)) {
            return getOrDefault(call.retry(), call.service());
        } else if (Objects.nonNull(postCall)) {
            return getOrDefault(postCall.retry(), postCall.service());
        } else if (Objects.nonNull(getCall)) {
            return getOrDefault(getCall.retry(), getCall.service());
        } else if (Objects.nonNull(patchCall)) {
            return getOrDefault(patchCall.retry(), patchCall.service());
        } else if (Objects.nonNull(deleteCall)) {
            return getOrDefault(deleteCall.retry(), deleteCall.service());
        } else if (Objects.nonNull(putCall)) {
            return getOrDefault(putCall.retry(), putCall.service());
        } else if (Objects.nonNull(unaryRPCCall)) {
            return getOrDefault(unaryRPCCall.retry(), unaryRPCCall.service());
        } else if (Objects.nonNull(bidiRPCCall)) {
            return getOrDefault(bidiRPCCall.retry(), bidiRPCCall.service());
        } else if (Objects.nonNull(serviceStreamRPCCall)) {
            return getOrDefault(serviceStreamRPCCall.retry(), serviceStreamRPCCall.service());
        } else if (Objects.nonNull(clientStreamRPCCall)) {
            return getOrDefault(clientStreamRPCCall.retry(), clientStreamRPCCall.service());
        }
        return "";
    }

    public String fallback() {
        if (Objects.nonNull(call)) {
            return call.fallback();
        } else if (Objects.nonNull(postCall)) {
            return postCall.fallback();
        } else if (Objects.nonNull(getCall)) {
            return getCall.fallback();
        } else if (Objects.nonNull(patchCall)) {
            return patchCall.fallback();
        } else if (Objects.nonNull(deleteCall)) {
            return deleteCall.fallback();
        } else if (Objects.nonNull(putCall)) {
            return putCall.fallback();
        } else if (Objects.nonNull(unaryRPCCall)) {
            return unaryRPCCall.fallback();
        } else if (Objects.nonNull(bidiRPCCall)) {
            return bidiRPCCall.fallback();
        } else if (Objects.nonNull(serviceStreamRPCCall)) {
            return serviceStreamRPCCall.fallback();
        } else if (Objects.nonNull(clientStreamRPCCall)) {
            return clientStreamRPCCall.fallback();
        }
        return "";
    }

    private String getOrDefault(String name, String def) {
        return StringUtils.hasLength(name) ? name : def;
    }
}
