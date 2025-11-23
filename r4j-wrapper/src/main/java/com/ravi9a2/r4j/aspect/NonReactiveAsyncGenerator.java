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

public class NonReactiveAsyncGenerator {

    private NonReactiveAsyncGenerator() {
    }

    public static StringBuilder imports(HashSet<ProcessorUtility.ExecutorType> executorTypes) {
        StringBuilder imports = new StringBuilder();
        if (ProcessorUtility.isNonReactiveAsync(executorTypes)) {
            return imports
                    .append("import org.apache.http.client.HttpClient;\n")
                    .append("import org.springframework.beans.factory.annotation.Qualifier;\n")
                    .append("import java.util.concurrent.CompletableFuture;\n")
                    .append("import java.util.concurrent.ThreadPoolExecutor;\n")
                    .append("import static com.ravi9a2.r4j.config.FallbackExecutorBeanLoader.FALLBACK_TASK_EXECUTOR;\n");
        }
        return imports;
    }

    public static StringBuilder memberFields() {
        return new StringBuilder()
                .append("\tprivate NonReactiveClientRegistry nonReactiveClientRegistry;\n")
                .append("\tprivate NonReactiveExecutor<?> nonReactiveExecutor;\n")
                .append("\tprivate ThreadPoolExecutor fallbackThreadPoolExecutor;\n");
    }

    public static StringBuilder dependencies() {
        return new StringBuilder()
                .append("\t\t\tNonReactiveClientRegistry nonReactiveClientRegistry,\n")
                .append("\t\t\tNonReactiveExecutor<?> nonReactiveExecutor,\n")
                .append("\t\t\t@Qualifier(FALLBACK_TASK_EXECUTOR) ThreadPoolExecutor fallbackThreadPoolExecutor");
    }

    public static StringBuilder setDependencies() {
        return new StringBuilder()
                .append("\t\tthis.nonReactiveClientRegistry = nonReactiveClientRegistry;\n")
                .append("\t\tthis.nonReactiveExecutor = nonReactiveExecutor;\n")
                .append("\t\tthis.fallbackThreadPoolExecutor = fallbackThreadPoolExecutor;\n");
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
        return generateMethodBodyString(parameters, queryParamVar, pathParamVar, callAnnot, requestObjectName,
                methodElement);
    }

    private static StringBuilder generateMethodBodyString(List<? extends VariableElement> parameters,
            List<VariableElement> queryParamVar, List<VariableElement> pathParamVar, AnnotationWrapper callAnnot,
            String requestObjectName, ExecutableElement methodElement) {
        String headers = ProcessorUtility.processHeader(parameters);
        String queryParams = ProcessorUtility.processQueryParam(queryParamVar);
        String pathParams = ProcessorUtility.processPathParam(pathParamVar);
        String fallbackMethod = callAnnot.fallback();
        String returnType = getReturnType(methodElement);
        String nonReactiveCallString = makeCall(callAnnot, fallbackMethod, requestObjectName, returnType,
                methodElement.getSimpleName().toString());
        String restCallDefinitionString = ProcessorUtility.buildRestCallDefinition(callAnnot, headers, queryParams,
                pathParams, returnType, requestObjectName);
        String logLines = ProcessorUtility.addLogLines(headers, queryParams, pathParams, requestObjectName);

        return new StringBuilder().append("{\n")
                .append(!pathParams.isEmpty() ? pathParams + "\n" : "")
                .append(!queryParams.isEmpty() ? queryParams + "\n" : "")
                .append(!headers.isEmpty() ? headers + "\n" : "")
                .append(StringUtils.hasLength(fallbackMethod)
                        ? "\t\tString fallbackMethod = \"" + fallbackMethod + "\";\n"
                        : "")
                .append(restCallDefinitionString)
                .append(logLines)
                .append(nonReactiveCallString)
                .append("\t}\n\n");
    }

    private static String getReturnType(ExecutableElement methodElement) {
        String rawReturnType = String.valueOf(methodElement.getReturnType());
        return rawReturnType.substring(rawReturnType.indexOf('<') + 1, rawReturnType.lastIndexOf('>'));
    }

    public static String makeCall(AnnotationWrapper callAnnot, String fallbackMethod, String request,
            String responseType, String methodName) {
        boolean isSilentFailure = callAnnot.isSilent();
        return new StringBuilder().append("\t\treturn ((NonReactiveExecutor<HttpClient>)nonReactiveExecutor)\n")
                .append("\t\t\t\t.").append("executeWithCompletionStage")
                .append("((NonReactiveClient<HttpClient>)(nonReactiveClientRegistry.client((\"")
                .append(callAnnot.service()).append("\"").append("))), cd)")
                .append(handleFailure(callAnnot, fallbackMethod, request, responseType, methodName, isSilentFailure))
                .toString();
    }

    private static StringBuilder handleFailure(AnnotationWrapper callAnnot, String fallbackMethod, String request,
            String responseType, String methodName, boolean isSilentFailure) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasLength(fallbackMethod)) {
            builder.append("\n\t\t\t\t.handleAsync((t, e) -> {")
                    .append("\t\t\t\t\t").append(recordLatency(callAnnot, methodName))
                    .append("\t\t\t\t\tif (e == null) {\n")
                    .append("\t\t\t\t\t\treturn (").append(responseType).append(") t;\n")
                    .append("\t\t\t\t\t} else if (e instanceof Status4XXException) {\n")
                    .append("\t\t\t\t\t\t").append(silencer(isSilentFailure))
                    .append("\t\t\t\t\t} else {\n")
                    .append("\t\t\t\t\t\tlog.error(\"Executing provided fallback: ").append(fallbackMethod)
                    .append("" + "\");\n")
                    .append("\t\t\t\t\t\ttry { \n")
                    .append("\t\t\t\t\t\t\treturn ").append(fallbackMethod).append("(").append(request).append(", ")
                    .append("cd.getHttpHeaders()).get();\n")
                    .append("\t\t\t\t\t\t} catch (Exception ex) {\n")
                    .append("\t\t\t\t\t\t\t").append(silencer(isSilentFailure))
                    .append("\t\t\t\t\t\t}\n")
                    .append("\t\t\t\t\t}\n")
                    .append("\t\t\t\t},fallbackThreadPoolExecutor);\n");
        } else {
            builder.append("\n\t\t\t\t.handle((t, e) -> {\n")
                    .append("\t\t\t\t\t").append(recordLatency(callAnnot, methodName))
                    .append("\t\t\t\t\tif (e == null) {\n")
                    .append("\t\t\t\t\t\treturn (").append(responseType).append(") t;\n")
                    .append("\t\t\t\t\t} else {\n")
                    .append("\t\t\t\t\t\t").append(silencer(isSilentFailure))
                    .append("\t\t\t\t\t}\n")
                    .append("\t\t\t\t});\n");
        }
        return builder;
    }

    private static StringBuilder recordLatency(AnnotationWrapper callAnnot, String methodName) {
        return new StringBuilder().append("Metrics.recordLatencyWithTags(\"path=").append(callAnnot.path())
                .append(",httpMethod=").append(callAnnot.method().toString())
                .append(",method=").append(methodName)
                .append(",ext_service=").append(callAnnot.service()).append("\"")
                .append(", (System.currentTimeMillis()-startTime));\n");
    }

    private static String silencer(boolean isSilentFailure) {
        return (isSilentFailure ? "return null" : "throw new NetworkClientException(e)") + ";\n";
    }
}
