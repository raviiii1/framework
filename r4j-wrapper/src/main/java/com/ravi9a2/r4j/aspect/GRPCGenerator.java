package com.ravi9a2.r4j.aspect;

import com.ravi9a2.nea.annotations.PathParam;
import com.ravi9a2.nea.annotations.Payload;
import com.ravi9a2.nea.annotations.QueryParam;
import org.springframework.util.StringUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static com.ravi9a2.r4j.aspect.ProcessorUtility.ExecutorType.REACTIVE_FLUX;
import static com.ravi9a2.r4j.aspect.ProcessorUtility.ExecutorType.REACTIVE_MONO;

public class GRPCGenerator {

    private GRPCGenerator() {
    }

    public static StringBuilder imports(HashSet<ProcessorUtility.ExecutorType> executorTypes) {
        StringBuilder imports = new StringBuilder();
        if (ProcessorUtility.isRPC(executorTypes)) {
            return imports.append("import com.ravi9a2.grpc.GrpcClientWrapper;\n")
                    .append("import com.ravi9a2.nea.core.RPCExecutor;\n")
                    .append("import com.ravi9a2.nea.core.data.RPCCallDefinition;\n")
                    .append("import com.ravi9a2.nea.core.data.RPCMethod;\n")
                    .append("import com.ravi9a2.nea.core.data.Type;\n")
                    .append("import com.ravi9a2.nca.RPCClientRegistry;");
        }
        return imports;
    }

    public static StringBuilder memberFields() {
        return new StringBuilder()
                .append("\tprivate final RPCClientRegistry grpcClientRegistry;\n")
                .append("\tprivate final RPCExecutor<?> rpcExecutor;\n");
    }

    public static StringBuilder dependencies() {
        return new StringBuilder()
                .append("\t\t\tRPCClientRegistry grpcClientRegistry,\n")
                .append("\t\t\tRPCExecutor<?> rpcExecutor");
    }

    public static StringBuilder setDependencies() {
        return new StringBuilder()
                .append("\t\tthis.grpcClientRegistry = grpcClientRegistry;\n")
                .append("\t\tthis.rpcExecutor = rpcExecutor;\n");
    }

    public static StringBuilder generateMethod(ExecutableElement methodElement, AnnotationWrapper callAnnot,
            ProcessorUtility.ExecutorType executorType) {
        StringBuilder methodAnnotations = generateMethodAnnotations();
        StringBuilder methodSignature = ProcessorUtility.generateMethodSignature(methodElement);
        StringBuilder methodBody = generateMethodBody(methodElement, executorType, callAnnot);
        return new StringBuilder().append(methodAnnotations).append(methodSignature).append(methodBody);
    }

    private static StringBuilder generateMethodAnnotations() {
        return new StringBuilder().append("\t@Override\n");
    }

    private static StringBuilder generateMethodBody(ExecutableElement methodElement,
            ProcessorUtility.ExecutorType executorType, AnnotationWrapper callAnnot) {
        List<? extends VariableElement> parameters = methodElement.getParameters();
        String requestObjectName = null;
        List<VariableElement> queryParamVar = new ArrayList<>();
        List<VariableElement> pathParamVar = new ArrayList<>();
        for (VariableElement parameter : parameters) {
            if (Objects.nonNull(parameter.getAnnotation(Payload.class))) {
                requestObjectName = ProcessorUtility.processPayload(parameter);
            } else if (Objects.nonNull(parameter.getAnnotation(QueryParam.class))) {
                queryParamVar.add(parameter);
            } else if (Objects.nonNull(parameter.getAnnotation(PathParam.class))) {
                pathParamVar.add(parameter);
            }
        }
        return generateMethodBodyString(executorType, parameters, queryParamVar, pathParamVar, callAnnot,
                requestObjectName, methodElement);
    }

    private static StringBuilder generateMethodBodyString(ProcessorUtility.ExecutorType executorType,
            List<? extends VariableElement> parameters, List<VariableElement> queryParamVar,
            List<VariableElement> pathParamVar, AnnotationWrapper callAnnot, String requestObjectName,
            ExecutableElement methodElement) {
        String headers = ProcessorUtility.processHeader(parameters);
        String fallbackMethod = callAnnot.fallback();
        String returnType = getReturnType(methodElement, executorType);
        String rawReturnType = String.valueOf(methodElement.getReturnType());
        String callString = makeCall(callAnnot, executorType, rawReturnType);
        String rpcCallDefinitionString = buildRPCCallDefinition(callAnnot, headers, returnType, requestObjectName);
        String logLines = addLogLines(headers, requestObjectName);

        return new StringBuilder().append("{\n")
                .append(!headers.isEmpty() ? headers + "\n" : "")
                .append(StringUtils.hasLength(fallbackMethod)
                        ? "\t\tString fallbackMethod = \"" + fallbackMethod + "\";\n"
                        : "")
                .append(rpcCallDefinitionString)
                .append(logLines)
                .append(callString)
                .append("\t}\n\n");
    }

    public static String buildRPCCallDefinition(AnnotationWrapper callAnnot, String headers, String returnType,
            String requestObjectName) {
        StringBuilder sb = new StringBuilder();
        return sb.append("\t\tRPCCallDefinition cd = RPCCallDefinition.builder()\n")
                .append("\t\t\t.isCircuitBreakerEnabled(").append(callAnnot.cbEnabled()).append(")\n")
                .append("\t\t\t.isBulkheadEnabled(").append(callAnnot.bhEnabled()).append(")\n")
                .append("\t\t\t.serviceTag(\"").append(callAnnot.service()).append("\")\n")
                .append("\t\t\t.isSilentFailure(").append(callAnnot.isSilent()).append(")\n")
                .append("\t\t\t.isRetryable(").append(callAnnot.isRetryable()).append(")\n")
                .append("\t\t\t.responseType(").append(returnType).append(".class)\n")
                .append("\t\t\t.payload(").append(requestObjectName).append(")\n")
                .append("\t\t\t.cbTag(\"").append(callAnnot.circuitBreaker()).append("\")\n")
                .append("\t\t\t.bhTag(\"").append(callAnnot.bulkhead()).append("\")\n")
                .append("\t\t\t.retryTag(\"").append(callAnnot.retry()).append("\")\n")
                .append(!headers.isEmpty() ? "\t\t\t.grpcHeaders(genHeadersMap)\n" : "")
                .append("\t\t\t.type(Type.").append(callAnnot.type().toString()).append(")\n")
                .append("\t\t\t.rpcMethod(RPCMethod.").append(callAnnot.rpcMethod().toString()).append(")\n")
                .append("\t\t\t.fqPackageName(\"").append(callAnnot.fqPackageName()).append("\")\n")
                .append("\t\t\t.className(\"").append(callAnnot.className()).append("\")\n")
                .append("\t\t\t.methodName(\"").append(callAnnot.methodName()).append("\")\n")
                .append("\t\t\t.build();\n").toString();
    }

    public static String addLogLines(String headers, String requestObjectName) {
        StringBuilder sb = new StringBuilder();
        return sb.append("\t\tlog.debug(\"External call:: \" + cd.getServiceTag());\n")
                .append("\t\tlog.debug(\"Request: \" + cd.getRpcMethod() + \":\" + cd.getFqPackageName() + \".\" + cd.getClassName() + \".\" + cd.getMethodName());\n")
                .append(!headers.isEmpty() ? "\t\tlog.debug(\"Headers: \" + cd.getGrpcHeaders());\n" : "")
                .append("\t\tlog.debug(\"Payload: \" + ").append(requestObjectName).append(");\n").toString();
    }

    private static String getReturnType(ExecutableElement methodElement, ProcessorUtility.ExecutorType executorType) {
        String rawReturnType = String.valueOf(methodElement.getReturnType());
        String returnType;
        if (REACTIVE_MONO.equals(executorType) || REACTIVE_FLUX.equals(executorType)) {
            returnType = rawReturnType.substring(rawReturnType.indexOf('<') + 1, rawReturnType.lastIndexOf('>'));
        } else {
            returnType = rawReturnType;
        }
        return returnType;
    }

    private static String makeCall(AnnotationWrapper callAnnot, ProcessorUtility.ExecutorType executorType,
            String rawReturnType) {
        String executorMethodName = ProcessorUtility.ExecutorType.GRPC_BLOCKING.equals(executorType) ? "execute"
                : "executeAsync";
        return new StringBuilder()
                .append("\t\treturn ((RPCExecutor<io.grpc.ManagedChannel>)rpcExecutor).").append(executorMethodName)
                .append("((GrpcClientWrapper)grpcClientRegistry.client(\"")
                .append(callAnnot.service()).append("\"").append("), cd);\n")
                .toString();

    }
}
