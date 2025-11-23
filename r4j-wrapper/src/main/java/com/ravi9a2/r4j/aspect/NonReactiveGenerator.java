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

public class NonReactiveGenerator {

    private NonReactiveGenerator() {
    }

    public static StringBuilder imports(HashSet<ProcessorUtility.ExecutorType> executorTypes) {
        StringBuilder imports = new StringBuilder();
        if (ProcessorUtility.isNonReactive(executorTypes)) {
            return imports
                    .append("import org.apache.http.client.HttpClient;\n")
                    .append("import org.springframework.beans.factory.annotation.Qualifier;\n")
                    .append("import java.util.concurrent.CompletableFuture;\n");
        }
        return imports;
    }

    public static StringBuilder memberFields() {
        return new StringBuilder()
                .append("\tprivate NonReactiveClientRegistry nonReactiveClientRegistry;\n")
                .append("\tprivate NonReactiveExecutor<?> nonReactiveExecutor;\n");
    }

    public static StringBuilder dependencies() {
        return new StringBuilder()
                .append("\t\t\tNonReactiveClientRegistry nonReactiveClientRegistry,\n")
                .append("\t\t\tNonReactiveExecutor<?> nonReactiveExecutor");

    }

    public static StringBuilder setDependencies() {
        return new StringBuilder()
                .append("\t\tthis.nonReactiveClientRegistry = nonReactiveClientRegistry;\n")
                .append("\t\tthis.nonReactiveExecutor = nonReactiveExecutor;\n");

    }

    public static StringBuilder generateMethod(ExecutableElement methodElement, AnnotationWrapper callAnnot, ProcessorUtility.ExecutorType executorType) {
        StringBuilder methodAnnotations = generateMethodAnnotations(methodElement, callAnnot);
        StringBuilder methodSignature = ProcessorUtility.generateMethodSignature(methodElement);
        StringBuilder methodBody = generateMethodBody(methodElement, callAnnot);
        return new StringBuilder().append(methodAnnotations).append(methodSignature).append(methodBody);
    }

    private static StringBuilder generateMethodAnnotations(ExecutableElement methodElement, AnnotationWrapper callAnnot) {
        StringBuilder methodAnnotations = new StringBuilder();
        methodAnnotations.append("\t@Override\n");
        ProcessorUtility.generateInstrumented(methodElement, callAnnot, methodAnnotations);
        return methodAnnotations;
    }

    private static StringBuilder generateMethodBody(ExecutableElement methodElement, AnnotationWrapper callAnnot) {
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
        return generateMethodBodyString(parameters, queryParamVar, pathParamVar, callAnnot, requestObjectName, methodElement);
    }

    private static StringBuilder generateMethodBodyString(List<? extends VariableElement> parameters, List<VariableElement> queryParamVar, List<VariableElement> pathParamVar, AnnotationWrapper callAnnot, String requestObjectName, ExecutableElement methodElement) {
        String headers = ProcessorUtility.processHeader(parameters);
        String queryParams = ProcessorUtility.processQueryParam(queryParamVar);
        String pathParams = ProcessorUtility.processPathParam(pathParamVar);
        String fallbackMethod = callAnnot.fallback();
        String returnType = getReturnType(methodElement);
        String nonReactiveCallString = makeCall(callAnnot);
        String restCallDefinitionString = ProcessorUtility.buildRestCallDefinition(callAnnot, headers, queryParams, pathParams, returnType, requestObjectName);
        String logLines = ProcessorUtility.addLogLines(headers, queryParams, pathParams, requestObjectName);

        return new StringBuilder().append("{\n")
                .append(!pathParams.isEmpty() ? pathParams + "\n" : "")
                .append(!queryParams.isEmpty() ? queryParams + "\n" : "")
                .append(!headers.isEmpty() ? headers + "\n" : "")
                .append(StringUtils.hasLength(fallbackMethod) ? "\t\tString fallbackMethod = \"" + fallbackMethod + "\";\n" : "")
                .append(restCallDefinitionString)
                .append(logLines)
                .append(nonReactiveCallString)
                .append("\t}\n\n");
    }

    private static String getReturnType(ExecutableElement methodElement) {
        return String.valueOf(methodElement.getReturnType());
    }

    public static String makeCall(AnnotationWrapper callAnnot) {
        boolean isSilentFailure = callAnnot.isSilent();
        return new StringBuilder().append("\t\ttry { \n")
                .append("\t\t\treturn ((NonReactiveExecutor<HttpClient>)nonReactiveExecutor)\n")
                .append("\t\t\t\t\t.").append("execute").append("((NonReactiveClient<HttpClient>)(nonReactiveClientRegistry.client((\"").append(callAnnot.service()).append("\"").append("))), cd);\n")
                .append("\t\t} catch (Exception ex) {\n")
                .append("\t\t\t" + (isSilentFailure ? "return null" : "throw ex") + ";\n")
                .append("\t\t}\n")
                .toString();
    }

}
