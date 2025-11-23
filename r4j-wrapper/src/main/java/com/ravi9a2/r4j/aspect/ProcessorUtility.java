package com.ravi9a2.r4j.aspect;

import com.ravi9a2.nea.annotations.BidiRPCCall;
import com.ravi9a2.nea.annotations.Call;
import com.ravi9a2.nea.annotations.ClientStreamRPCCall;
import com.ravi9a2.nea.annotations.DeleteCall;
import com.ravi9a2.nea.annotations.GetCall;
import com.ravi9a2.nea.annotations.Header;
import com.ravi9a2.nea.annotations.Headers;
import com.ravi9a2.nea.annotations.PatchCall;
import com.ravi9a2.nea.annotations.PathParam;
import com.ravi9a2.nea.annotations.PostCall;
import com.ravi9a2.nea.annotations.PutCall;
import com.ravi9a2.nea.annotations.QueryParam;
import com.ravi9a2.nea.annotations.ServiceStreamRPCCall;
import com.ravi9a2.nea.annotations.UnaryRPCCall;
import com.ravi9a2.nea.core.data.Type;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ProcessorUtility {

    public static final String MONO = "reactor.core.publisher.Mono";
    public static final String FLUX = "reactor.core.publisher.Flux";
    public static final String COMPLETION_STAGE = "java.util.concurrent.CompletionStage";
    public static final String COMPLETABLE_FUTURE = "java.util.concurrent.CompletableFuture";
    public static final String LISTENABLE_FUTURE = "com.google.common.util.concurrent.ListenableFuture";

    public static AnnotationWrapper readAnnotation(ExecutableElement method) {
        PostCall postCall = method.getAnnotation(PostCall.class);
        if (Objects.nonNull(postCall)) {
            return AnnotationWrapper.wrap(postCall);
        }

        GetCall getCall = method.getAnnotation(GetCall.class);
        if (Objects.nonNull(getCall)) {
            return AnnotationWrapper.wrap(getCall);
        }

        PutCall putCall = method.getAnnotation(PutCall.class);
        if (Objects.nonNull(putCall)) {
            return AnnotationWrapper.wrap(putCall);
        }

        DeleteCall deleteCall = method.getAnnotation(DeleteCall.class);
        if (Objects.nonNull(deleteCall)) {
            return AnnotationWrapper.wrap(deleteCall);
        }

        PatchCall patchCall = method.getAnnotation(PatchCall.class);
        if (Objects.nonNull(patchCall)) {
            return AnnotationWrapper.wrap(patchCall);
        }

        BidiRPCCall bidiRPCCall = method.getAnnotation(BidiRPCCall.class);
        if (Objects.nonNull(bidiRPCCall)) {
            return AnnotationWrapper.wrap(bidiRPCCall);
        }

        ClientStreamRPCCall clientStreamRPCCall = method.getAnnotation(ClientStreamRPCCall.class);
        if (Objects.nonNull(clientStreamRPCCall)) {
            return AnnotationWrapper.wrap(clientStreamRPCCall);
        }

        ServiceStreamRPCCall serviceStreamRPCCall = method.getAnnotation(ServiceStreamRPCCall.class);
        if (Objects.nonNull(serviceStreamRPCCall)) {
            return AnnotationWrapper.wrap(serviceStreamRPCCall);
        }

        UnaryRPCCall unaryRPCCall = method.getAnnotation(UnaryRPCCall.class);
        if (Objects.nonNull(unaryRPCCall)) {
            return AnnotationWrapper.wrap(unaryRPCCall);
        }

        Call call = method.getAnnotation(Call.class);
        if (Objects.nonNull(call)) {
            return AnnotationWrapper.wrap(call);
        }

        throw new IllegalArgumentException("Unrecognized call annotation.");
    }

    public static String processPathParam(List<VariableElement> parameters) {
        if (parameters.isEmpty()) {
            return "";
        }
        StringBuilder map = new StringBuilder("\t\tMap<String, String> genPathParamMap = new HashMap<>();");
        for (VariableElement parameter : parameters) {
            PathParam pathParam = parameter.getAnnotation(PathParam.class);
            String pathParaname = parameter.getSimpleName().toString();
            map.append("\n").append("\t\tgenPathParamMap.put(\"").append(pathParam.value())
                    .append("\", String.valueOf(").append(pathParaname).append("));");
        }
        return map.append("\n").toString();
    }

    public static String processHeader(List<? extends VariableElement> parameters) {
        if (parameters.isEmpty()) {
            return "";
        }
        StringBuilder map = new StringBuilder("\t\tMap<String, String> genHeadersMap = new HashMap<>();");
        for (VariableElement parameter : parameters) {
            if (Objects.nonNull(parameter.getAnnotation(Header.class))) {
                Header header = parameter.getAnnotation(Header.class);
                String headerName = parameter.getSimpleName().toString();
                map.append("\n").append("\t\tgenHeadersMap.put(\"").append(header.value()).append("\", String.valueOf(")
                        .append(headerName).append("));");
            } else if (Objects.nonNull(parameter.getAnnotation(Headers.class))) {
                String headersMapName = parameter.getSimpleName().toString();
                map.append("\n").append("\t\tgenHeadersMap.putAll(" + headersMapName + ");");
            }
        }
        return map.append("\n").toString();
    }

    public static String processQueryParam(List<VariableElement> parameters) {
        if (parameters.isEmpty()) {
            return "";
        }
        StringBuilder map = new StringBuilder("\t\tMap<String, String> genQueryParamMap = new HashMap<>();");
        for (VariableElement parameter : parameters) {
            QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
            String queryParaname = parameter.getSimpleName().toString();
            map.append("\n").append("\t\tgenQueryParamMap.put(\"").append(queryParam.value())
                    .append("\", String.valueOf(").append(queryParaname).append("));");
        }
        return map.append("\n").toString();
    }

    public static String processPayload(VariableElement parameter) {
        return parameter.getSimpleName().toString();
    }

    public static StringBuilder openClass(TypeElement classElement,
            HashSet<ProcessorUtility.ExecutorType> executorTypes) {
        return new StringBuilder().append("package ").append(classElement.getEnclosingElement().toString())
                .append(";\n\n")
                .append("import com.ravi9a2.nea.core.*;\n")
                .append("import com.ravi9a2.nca.*;\n")
                .append("import com.ravi9a2.nca.exceptions.Status4XXException;\n")
                .append("import com.ravi9a2.nca.exceptions.NetworkClientException;\n")
                .append("import com.ravi9a2.nea.core.data.*;\n")
                .append("import com.ravi9a2.instrumentation.enums.MetricType;\n")
                .append("import com.ravi9a2.instrumentation.annotation.Instrumented;\n")
                .append("import com.ravi9a2.r4j.Metrics;\n")
                .append("import org.springframework.beans.factory.annotation.Autowired;\n")
                .append("import org.springframework.util.StringUtils;\n")
                .append("import org.springframework.stereotype.Service;\n\n")
                .append("import java.util.*;\n\n")
                .append("import java.util.concurrent.ExecutionException;\n")
                .append(ReactiveGenerator.imports(executorTypes))
                .append(NonReactiveGenerator.imports(executorTypes))
                .append(NonReactiveAsyncGenerator.imports(executorTypes))
                .append(GRPCGenerator.imports(executorTypes))
                .append("\n")
                .append("@Service\n")
                .append("@lombok.extern.slf4j.Slf4j\n")
                .append("public class ").append(classElement.getSimpleName().toString()).append("Impl implements ")
                .append(classElement.getSimpleName().toString()).append(" {\n\n");
    }

    public static StringBuilder injectDependencies(TypeElement classElement,
            HashSet<ProcessorUtility.ExecutorType> executorTypes) {
        boolean isReactive = ProcessorUtility.isReactive(executorTypes);
        boolean isNonReactive = ProcessorUtility.isNonReactive(executorTypes);
        boolean isNonReactiveAsync = ProcessorUtility.isNonReactiveAsync(executorTypes);
        boolean isRPC = ProcessorUtility.isRPC(executorTypes);
        StringBuilder sb = new StringBuilder();
        memberFields(isReactive, isNonReactive, isNonReactiveAsync, isRPC, sb);
        sb.append(autowireConstructor(classElement));
        dependencies(isReactive, isNonReactive, isNonReactiveAsync, isRPC, sb);
        setDependencies(isReactive, isNonReactive, isNonReactiveAsync, isRPC, sb);
        sb.append("\t}\n\n");
        return sb;
    }

    private static void memberFields(boolean isReactive, boolean isNonReactive, boolean isNonReactiveAsync,
            boolean isRPC, StringBuilder sb) {
        if (isReactive) {
            sb.append(ReactiveGenerator.memberFields());
        }
        if (isNonReactive) {
            sb.append(NonReactiveGenerator.memberFields());
        }
        if (isNonReactiveAsync) {
            sb.append(NonReactiveAsyncGenerator.memberFields());
        }
        if (isRPC) {
            sb.append(GRPCGenerator.memberFields());
        }
    }

    private static void dependencies(boolean isReactive, boolean isNonReactive, boolean isNonReactiveAsync,
            boolean isRPC, StringBuilder sb) {
        if (isReactive) {
            sb.append(ReactiveGenerator.dependencies());
            sb.append(isNonReactive || isNonReactiveAsync || isRPC ? ",\n" : "");
        }
        if (isNonReactive) {
            sb.append(NonReactiveGenerator.dependencies());
            sb.append(isNonReactiveAsync || isRPC ? ",\n" : "");
        }
        if (isNonReactiveAsync) {
            sb.append(NonReactiveAsyncGenerator.dependencies());
            sb.append(isRPC ? ",\n" : "");
        }
        if (isRPC) {
            sb.append(GRPCGenerator.dependencies());
        }
        sb.append(") {\n");
    }

    private static void setDependencies(boolean isReactive, boolean isNonReactive, boolean isNonReactiveAsync,
            boolean isRPC, StringBuilder sb) {
        if (isReactive) {
            sb.append(ReactiveGenerator.setDependencies());
        }
        if (isNonReactive) {
            sb.append(NonReactiveGenerator.setDependencies());
        }
        if (isNonReactiveAsync) {
            sb.append(NonReactiveAsyncGenerator.setDependencies());
        }
        if (isRPC) {
            sb.append(GRPCGenerator.setDependencies());
        }
    }

    private static StringBuilder autowireConstructor(TypeElement classElement) {
        return new StringBuilder()
                .append("\n")
                .append("\t@Autowired\n")
                .append("\tpublic ").append(classElement.getSimpleName().toString()).append("Impl(\n");
    }

    public static void generateInstrumented(ExecutableElement methodElement, AnnotationWrapper callAnnot,
            StringBuilder stringBuilder) {
        stringBuilder.append("\t@Instrumented(metricType = MetricType.HTTP,")
                .append(" tagSet = \"path=").append(callAnnot.path()).append(",httpMethod=")
                .append(callAnnot.method().toString())
                .append(",method=").append(methodElement.getSimpleName().toString())
                .append(",service=").append(callAnnot.service()).append("\")\n");
    }

    public static StringBuilder generateMethodSignature(ExecutableElement methodElement) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\tpublic ").append(methodElement.getReturnType().toString()).append(" ")
                .append(methodElement.getSimpleName().toString()).append("(");

        // Append method parameters
        for (int i = 0; i < methodElement.getParameters().size(); i++) {
            stringBuilder.append(methodElement.getParameters().get(i).asType().toString() + " "
                    + methodElement.getParameters().get(i).getSimpleName().toString());
            if (i < methodElement.getParameters().size() - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(") ");
        return stringBuilder;
    }

    public static String buildRestCallDefinition(AnnotationWrapper callAnnot, String headers, String queryParams,
            String pathParams, String returnType, String requestObjectName) {
        String responseTypeCode = getResponseTypeCode(returnType);
        return new StringBuilder()
                .append("\t\tRestCallDefinition cd = RestCallDefinition.builder()\n")
                .append("\t\t\t.isCircuitBreakerEnabled(").append(callAnnot.cbEnabled()).append(")\n")
                .append("\t\t\t.isBulkheadEnabled(").append(callAnnot.bhEnabled()).append(")\n")
                .append("\t\t\t.serviceTag(\"").append(callAnnot.service()).append("\")\n")
                .append("\t\t\t.isSilentFailure(").append(callAnnot.isSilent()).append(")\n")
                .append("\t\t\t.isRetryable(").append(callAnnot.isRetryable()).append(")\n")
                .append("\t\t\t.path(\"").append(callAnnot.path()).append("\")\n")
                .append("\t\t\t.responseType(").append(responseTypeCode).append(")\n")
                .append("\t\t\t.payload(").append(requestObjectName).append(")\n")
                .append("\t\t\t.cbTag(\"").append(callAnnot.circuitBreaker()).append("\")\n")
                .append("\t\t\t.bhTag(\"").append(callAnnot.bulkhead()).append("\")\n")
                .append("\t\t\t.retryTag(\"").append(callAnnot.retry()).append("\")\n")
                .append(!headers.isEmpty() ? "\t\t\t.httpHeaders(genHeadersMap)\n" : "")
                .append(!pathParams.isEmpty() ? "\t\t\t.pathParams(genPathParamMap)\n" : "")
                .append(!queryParams.isEmpty() ? "\t\t\t.queryParams(genQueryParamMap)\n" : "")
                .append("\t\t\t.type(Type.").append(callAnnot.type().toString()).append(")\n")
                .append("\t\t\t.httpMethod(HTTPMethod.").append(callAnnot.method().toString()).append(")\n")
                .append("\t\t\t.build();\n").toString();
    }

    private static String getResponseTypeCode(String returnType) {
        // Handle parameterized types like List<User>
        if (returnType.contains("<") && returnType.contains(">")) {
            // Extract the raw type and type arguments
            int openBracket = returnType.indexOf('<');
            int closeBracket = returnType.lastIndexOf('>');
            String rawType = returnType.substring(0, openBracket).trim();
            String typeArg = returnType.substring(openBracket + 1, closeBracket).trim();

            // Create a ParameterizedType using TypeReference pattern
            return "new java.lang.reflect.ParameterizedType() {\n" +
                    "\t\t\t\t@Override\n" +
                    "\t\t\t\tpublic java.lang.reflect.Type[] getActualTypeArguments() {\n" +
                    "\t\t\t\t\treturn new java.lang.reflect.Type[]{" + typeArg + ".class};\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t\t@Override\n" +
                    "\t\t\t\tpublic java.lang.reflect.Type getRawType() {\n" +
                    "\t\t\t\t\treturn " + rawType + ".class;\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t\t@Override\n" +
                    "\t\t\t\tpublic java.lang.reflect.Type getOwnerType() {\n" +
                    "\t\t\t\t\treturn null;\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t}";
        } else {
            // Simple type, use .class
            return returnType + ".class";
        }
    }

    public static String addLogLines(String headers, String queryParams, String pathParams, String requestObjectName) {
        StringBuilder sb = new StringBuilder();
        return sb.append("\t\tlog.debug(\"External call:: \" + cd.getServiceTag());\n")
                .append("\t\tlog.debug(\"Request: \" + cd.getHttpMethod() + \":\" + cd.getPath());\n")
                .append(!headers.isEmpty() ? "\t\tlog.debug(\"Headers: \" + cd.getHttpHeaders());\n" : "")
                .append(!pathParams.isEmpty() ? "\t\tlog.debug(\"PathParams: \" + cd.getPathParams());\n" : "")
                .append(!queryParams.isEmpty() ? "\t\tlog.debug(\"QueryParams: \" + cd.getQueryParams());\n" : "")
                .append("\t\tlog.debug(\"Payload: \" + ").append(requestObjectName).append(");\n").toString();
    }

    public static ExecutorType getExecutorType(ExecutableElement methodElement, Type type) {
        String rawReturnType = methodElement.getReturnType().toString();
        ExecutorType executorMethodName;
        String wrapperReturn = "";
        if (rawReturnType.contains("<")) {
            wrapperReturn = rawReturnType.substring(0, rawReturnType.indexOf('<'));
        }
        if (Type.HTTP.equals(type) && wrapperReturn.equals(MONO)) {
            executorMethodName = ExecutorType.REACTIVE_MONO;
        } else if (Type.HTTP.equals(type) && wrapperReturn.equals(FLUX)) {
            executorMethodName = ExecutorType.REACTIVE_FLUX;
        } else if (Type.HTTP.equals(type)
                && (wrapperReturn.equals(COMPLETION_STAGE) || wrapperReturn.equals(COMPLETABLE_FUTURE))) {
            executorMethodName = ExecutorType.NON_REACTIVE_ASYNC;
        } else if (Type.HTTP.equals(type)) {
            executorMethodName = ExecutorType.NON_REACTIVE;
        } else if (Type.RPC.equals(type) && wrapperReturn.equals(LISTENABLE_FUTURE)) {
            executorMethodName = ExecutorType.GRPC_FUTURE;
        } else if (Type.RPC.equals(type)) {
            executorMethodName = ExecutorType.GRPC_BLOCKING;
        } else {
            throw new IllegalArgumentException("Unrecognized executor-type.");
        }
        return executorMethodName;
    }

    public static String getReturnType(ExecutableElement methodElement) {
        String rawReturnType = String.valueOf(methodElement.getReturnType());
        String returnType;
        if (rawReturnType.contains("<")) {
            String wrapperReturn = rawReturnType.substring(0, rawReturnType.indexOf('<'));
            if (wrapperReturn.equals("reactor.core.publisher.Mono")
                    || wrapperReturn.equals("reactor.core.publisher.Flux")
                    || wrapperReturn.equals("java.util.concurrent.CompletableFuture")) {
                returnType = rawReturnType.substring(rawReturnType.indexOf('<') + 1, rawReturnType.lastIndexOf('>'));
            } else {
                returnType = rawReturnType;
            }
        } else {
            returnType = rawReturnType;
        }
        return returnType;
    }

    public static StringBuilder closeClass() {
        return new StringBuilder().append("}\n");
    }

    public static boolean isReactive(HashSet<ExecutorType> executorTypes) {
        return executorTypes.contains(ExecutorType.REACTIVE_FLUX) || executorTypes.contains(ExecutorType.REACTIVE_MONO);
    }

    public static boolean isNonReactiveAsync(HashSet<ExecutorType> executorTypes) {
        return executorTypes.contains(ExecutorType.NON_REACTIVE_ASYNC);
    }

    public static boolean isNonReactive(HashSet<ExecutorType> executorTypes) {
        return executorTypes.contains(ExecutorType.NON_REACTIVE);
    }

    public static boolean isRPC(HashSet<ExecutorType> executorTypes) {
        return executorTypes.contains(ExecutorType.GRPC_FUTURE) || executorTypes.contains(ExecutorType.GRPC_BLOCKING);
    }

    public enum ExecutorType {
        REACTIVE_MONO,
        REACTIVE_FLUX,
        NON_REACTIVE,
        NON_REACTIVE_ASYNC,
        GRPC_BLOCKING,
        GRPC_FUTURE;
    }

}
