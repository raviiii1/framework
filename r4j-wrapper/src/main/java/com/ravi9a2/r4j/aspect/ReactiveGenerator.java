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

public class ReactiveGenerator {

    private ReactiveGenerator() {
    }

    public static StringBuilder imports(HashSet<ProcessorUtility.ExecutorType> executorTypes) {
        StringBuilder imports = new StringBuilder();
        if (ProcessorUtility.isReactive(executorTypes)) {
            return imports.append("import org.springframework.web.reactive.function.client.WebClient;\n");
        }
        return imports;
    }

    public static StringBuilder memberFields() {
        return new StringBuilder()
                .append("\tprivate ReactiveClientRegistry reactiveClientRegistry;\n")
                .append("\tprivate ReactiveExecutor<?> reactiveExecutor;\n");
    }

    public static StringBuilder dependencies() {
        return new StringBuilder()
                .append("\t\t\tReactiveClientRegistry reactiveClientRegistry,\n")
                .append("\t\t\tReactiveExecutor<?> reactiveExecutor");
    }

    public static StringBuilder setDependencies() {
        return new StringBuilder()
                .append("\t\tthis.reactiveClientRegistry = reactiveClientRegistry;\n")
                .append("\t\tthis.reactiveExecutor = reactiveExecutor;\n");
    }

    public static StringBuilder generateMethod(ExecutableElement methodElement, AnnotationWrapper callAnnot, ProcessorUtility.ExecutorType executorType) {
        StringBuilder methodAnnotations = generateMethodAnnotations();
        StringBuilder methodSignature = ProcessorUtility.generateMethodSignature(methodElement);
        StringBuilder methodBody = generateMethodBody(methodElement, executorType, callAnnot);
        return new StringBuilder().append(methodAnnotations).append(methodSignature).append(methodBody);
    }

    private static StringBuilder generateMethodAnnotations() {
        return new StringBuilder().append("\t@Override\n");
    }

    private static StringBuilder generateMethodBody(ExecutableElement methodElement, ProcessorUtility.ExecutorType executorType, AnnotationWrapper callAnnot) {
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
        return generateMethodBodyString(executorType, parameters, queryParamVar, pathParamVar, callAnnot, requestObjectName, methodElement);
    }

    private static StringBuilder generateMethodBodyString(ProcessorUtility.ExecutorType executorType, List<? extends VariableElement> parameters, List<VariableElement> queryParamVar, List<VariableElement> pathParamVar, AnnotationWrapper callAnnot, String requestObjectName, ExecutableElement methodElement) {
        String headers = ProcessorUtility.processHeader(parameters);
        String queryParams = ProcessorUtility.processQueryParam(queryParamVar);
        String pathParams = ProcessorUtility.processPathParam(pathParamVar);
        String fallbackMethod = callAnnot.fallback();
        String returnType = getReturnType(methodElement, executorType);
        String rawReturnType = String.valueOf(methodElement.getReturnType());
        String reactiveCallString = makeCall(callAnnot, executorType, rawReturnType);
        String restCallDefinitionString = ProcessorUtility.buildRestCallDefinition(callAnnot, headers, queryParams, pathParams, returnType, requestObjectName);
        String logLines = ProcessorUtility.addLogLines(headers, queryParams, pathParams, requestObjectName);

        return new StringBuilder().append("{\n")
                .append(!pathParams.isEmpty() ? pathParams + "\n" : "")
                .append(!queryParams.isEmpty() ? queryParams + "\n" : "")
                .append(!headers.isEmpty() ? headers + "\n" : "")
                .append(StringUtils.hasLength(fallbackMethod) ? "\t\tString fallbackMethod = \"" + fallbackMethod + "\";\n" : "")
                .append(restCallDefinitionString)
                .append(logLines)
                .append(reactiveCallString)
                .append("\t}\n\n");
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

    public static String makeCall(AnnotationWrapper callAnnot, ProcessorUtility.ExecutorType executorType, String rawReturnType) {
        String executorMethodName = ProcessorUtility.ExecutorType.REACTIVE_FLUX.equals(executorType) ? "executeToFlux" : "executeToMono";
        return new StringBuilder().append("\t\tString tags = \"client=")
                .append(callAnnot.service()).append(",path=").append(callAnnot.path()).append(",method=").append(callAnnot.method()).append("\";\n")
                .append("\t\t").append(rawReturnType).append(" val = ((ReactiveExecutor<WebClient>)reactiveExecutor)\n")
                .append("\t\t\t\t.").append(executorMethodName).append("((ReactiveClient<WebClient>)reactiveClientRegistry.client(\"").append(callAnnot.service()).append("\"").append("), cd);\n")
                .append("\t\treturn com.ravi9a2.r4j.Metrics.latency(val, tags);\n")
                .toString();
    }
}
