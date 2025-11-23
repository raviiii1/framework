package com.ravi9a2.r4j.aspect;

import com.ravi9a2.nea.annotations.GetCall;
import com.ravi9a2.nea.annotations.Header;
import com.ravi9a2.nea.annotations.Headers;
import com.ravi9a2.nea.annotations.PathParam;
import com.ravi9a2.nea.annotations.PostCall;
import com.ravi9a2.nea.annotations.QueryParam;
import com.ravi9a2.nea.core.data.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TestProcessorUtility {
    @Test
    public void testReadAnnotation() {

        ExecutableElement executableElement = Mockito.mock(ExecutableElement.class);

        GetCall getCall = Mockito.mock(GetCall.class);

        Mockito.when(executableElement.getAnnotation(PostCall.class)).thenReturn(null);
        Mockito.when(executableElement.getAnnotation(GetCall.class)).thenReturn(getCall);
        Mockito.when(getCall.service()).thenReturn("myservice");

        AnnotationWrapper result = ProcessorUtility.readAnnotation(executableElement);

        Mockito.verify(executableElement, Mockito.times(1)).getAnnotation(PostCall.class);
        Mockito.verify(executableElement, Mockito.times(1)).getAnnotation(GetCall.class);

        String service = result.service();

        Assertions.assertEquals(service, "myservice");
    }

    @Test
    public void testProcessPathParam_TwoPathParams() {

        VariableElement variableElement1 = Mockito.mock(VariableElement.class);
        VariableElement variableElement2 = Mockito.mock(VariableElement.class);
        List<VariableElement> variableElements = new ArrayList<>();
        variableElements.add(variableElement1);
        variableElements.add(variableElement2);

        PathParam pathParam1 = Mockito.mock(PathParam.class);
        PathParam pathParam2 = Mockito.mock(PathParam.class);
        Name name1 = Mockito.mock(Name.class);
        Name name2 = Mockito.mock(Name.class);

        Mockito.when(variableElement1.getAnnotation(PathParam.class)).thenReturn(pathParam1);
        Mockito.when(variableElement2.getAnnotation(PathParam.class)).thenReturn(pathParam2);
        Mockito.when(pathParam1.value()).thenReturn("pathParam1");
        Mockito.when(pathParam2.value()).thenReturn("pathParam2");
        Mockito.when(variableElement1.getSimpleName()).thenReturn(name1);
        Mockito.when(variableElement2.getSimpleName()).thenReturn(name2);
        Mockito.when(name1.toString()).thenReturn("pathParamValue1");
        Mockito.when(name2.toString()).thenReturn("pathParamValue2");

        String actual = ProcessorUtility.processPathParam(variableElements);

        Assertions.assertEquals("\t\tMap<String, String> genPathParamMap = new HashMap<>();\n" +
                "\t\tgenPathParamMap.put(\"pathParam1\", String.valueOf(pathParamValue1));\n" +
                "\t\tgenPathParamMap.put(\"pathParam2\", String.valueOf(pathParamValue2));\n", actual);
    }

    @Test
    public void testProcessPathParam_NoPathParams() {

        List<VariableElement> variableElements = new ArrayList<>();

        String actual = ProcessorUtility.processPathParam(variableElements);

        Assertions.assertEquals("", actual);
    }

    @Test
    public void testProcessQueryParam_TwoQueryParams() {

        VariableElement variableElement1 = Mockito.mock(VariableElement.class);
        VariableElement variableElement2 = Mockito.mock(VariableElement.class);
        List<VariableElement> variableElements = new ArrayList<>();
        variableElements.add(variableElement1);
        variableElements.add(variableElement2);

        QueryParam queryParam1 = Mockito.mock(QueryParam.class);
        QueryParam queryParam2 = Mockito.mock(QueryParam.class);
        Name name1 = Mockito.mock(Name.class);
        Name name2 = Mockito.mock(Name.class);

        Mockito.when(variableElement1.getAnnotation(QueryParam.class)).thenReturn(queryParam1);
        Mockito.when(variableElement2.getAnnotation(QueryParam.class)).thenReturn(queryParam2);
        Mockito.when(queryParam1.value()).thenReturn("queryParam1");
        Mockito.when(queryParam2.value()).thenReturn("queryParam2");
        Mockito.when(variableElement1.getSimpleName()).thenReturn(name1);
        Mockito.when(variableElement2.getSimpleName()).thenReturn(name2);
        Mockito.when(name1.toString()).thenReturn("queryParamValue1");
        Mockito.when(name2.toString()).thenReturn("queryParamValue2");

        String actual = ProcessorUtility.processQueryParam(variableElements);

        Assertions.assertEquals("\t\tMap<String, String> genQueryParamMap = new HashMap<>();\n" +
                "\t\tgenQueryParamMap.put(\"queryParam1\", String.valueOf(queryParamValue1));\n" +
                "\t\tgenQueryParamMap.put(\"queryParam2\", String.valueOf(queryParamValue2));\n", actual);
    }

    @Test
    public void testProcessQueryParam_NoQueryParams() {

        List<VariableElement> variableElements = new ArrayList<>();

        String actual = ProcessorUtility.processQueryParam(variableElements);

        Assertions.assertEquals("", actual);
    }

    @Test
    public void testProcessHeader_HeaderAndHeaders() {

        VariableElement variableElement1 = Mockito.mock(VariableElement.class);
        VariableElement variableElement2 = Mockito.mock(VariableElement.class);
        VariableElement variableElement3 = Mockito.mock(VariableElement.class);
        List<VariableElement> variableElements = new ArrayList<>();
        variableElements.add(variableElement1);
        variableElements.add(variableElement2);
        variableElements.add(variableElement3);

        Header header1 = Mockito.mock(Header.class);
        Header header2 = Mockito.mock(Header.class);
        Headers headers = Mockito.mock(Headers.class);
        Name name1 = Mockito.mock(Name.class);
        Name name2 = Mockito.mock(Name.class);
        Name name = Mockito.mock(Name.class);

        Mockito.when(variableElement1.getAnnotation(Header.class)).thenReturn(header1);
        Mockito.when(variableElement2.getAnnotation(Header.class)).thenReturn(header2);
        Mockito.when(variableElement3.getAnnotation(Headers.class)).thenReturn(headers);
        Mockito.when(header1.value()).thenReturn("header1");
        Mockito.when(header2.value()).thenReturn("header2");
        Mockito.when(variableElement1.getSimpleName()).thenReturn(name1);
        Mockito.when(variableElement2.getSimpleName()).thenReturn(name2);
        Mockito.when(variableElement3.getSimpleName()).thenReturn(name);
        Mockito.when(name1.toString()).thenReturn("headerValue1");
        Mockito.when(name2.toString()).thenReturn("headerValue2");
        Mockito.when(name.toString()).thenReturn("headersValueMap");

        String actual = ProcessorUtility.processHeader(variableElements);

        Assertions.assertEquals("\t\tMap<String, String> genHeadersMap = new HashMap<>();\n" +
                "\t\tgenHeadersMap.put(\"header1\", String.valueOf(headerValue1));\n" +
                "\t\tgenHeadersMap.put(\"header2\", String.valueOf(headerValue2));\n" +
                "\t\tgenHeadersMap.putAll(headersValueMap);\n", actual);
    }

    @Test
    public void testProcessHeader_NoHeaders() {

        List<VariableElement> variableElements = new ArrayList<>();

        String actual = ProcessorUtility.processHeader(variableElements);

        Assertions.assertEquals("", actual);
    }

    @Test
    public void testOpenClass_nonReactive() throws IOException {
        TypeElement classElement = Mockito.mock(TypeElement.class);
        Element element = Mockito.mock(Element.class);
        Name name = Mockito.mock(Name.class);

        Mockito.when(classElement.getEnclosingElement()).thenReturn(element);
        Mockito.when(element.toString()).thenReturn("com.ravi9a2.r4j");
        Mockito.when(classElement.getSimpleName()).thenReturn(name);
        Mockito.when(name.toString()).thenReturn("ClassName");

        HashSet<ProcessorUtility.ExecutorType> types = new HashSet<>();
        types.add(ProcessorUtility.ExecutorType.NON_REACTIVE_ASYNC);
        StringBuilder actual = ProcessorUtility.openClass(classElement, types);

        Assertions.assertEquals("package com.ravi9a2.r4j;\n" +
                "\n" +
                "import com.ravi9a2.nea.core.*;\n" +
                "import com.ravi9a2.nca.*;\n" +
                "import com.ravi9a2.nca.exceptions.Status4XXException;\n" +
                "import com.ravi9a2.nca.exceptions.NetworkClientException;\n" +
                "import com.ravi9a2.nea.core.data.*;\n" +
                "import com.ravi9a2.instrumentation.enums.MetricType;\n" +
                "import com.ravi9a2.instrumentation.annotation.Instrumented;\n" +
                "import com.ravi9a2.r4j.Metrics;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.util.StringUtils;\n" +
                "import org.springframework.stereotype.Service;\n" +
                "\n" +
                "import java.util.*;\n" +
                "\n" +
                "import java.util.concurrent.ExecutionException;\n" +
                "import org.apache.http.client.HttpClient;\n" +
                "import org.springframework.beans.factory.annotation.Qualifier;\n" +
                "import java.util.concurrent.CompletableFuture;\n" +
                "import java.util.concurrent.ThreadPoolExecutor;\n" +
                "import static com.ravi9a2.r4j.config.FallbackExecutorBeanLoader.FALLBACK_TASK_EXECUTOR;\n" +
                "\n" +
                "@Service\n" +
                "@lombok.extern.slf4j.Slf4j\n" +
                "public class ClassNameImpl implements ClassName {\n" +
                "\n", actual.toString());
    }

    @Test
    public void testOpenClass_reactive() throws IOException {
        TypeElement classElement = Mockito.mock(TypeElement.class);
        Element element = Mockito.mock(Element.class);
        Name name = Mockito.mock(Name.class);

        Mockito.when(classElement.getEnclosingElement()).thenReturn(element);
        Mockito.when(element.toString()).thenReturn("com.ravi9a2.r4j");
        Mockito.when(classElement.getSimpleName()).thenReturn(name);
        Mockito.when(name.toString()).thenReturn("ClassName");

        HashSet<ProcessorUtility.ExecutorType> types = new HashSet<>();
        types.add(ProcessorUtility.ExecutorType.REACTIVE_MONO);
        StringBuilder actual = ProcessorUtility.openClass(classElement, types);

        Assertions.assertEquals("package com.ravi9a2.r4j;\n" +
                "\n" +
                "import com.ravi9a2.nea.core.*;\n" +
                "import com.ravi9a2.nca.*;\n" +
                "import com.ravi9a2.nca.exceptions.Status4XXException;\n" +
                "import com.ravi9a2.nca.exceptions.NetworkClientException;\n" +
                "import com.ravi9a2.nea.core.data.*;\n" +
                "import com.ravi9a2.instrumentation.enums.MetricType;\n" +
                "import com.ravi9a2.instrumentation.annotation.Instrumented;\n" +
                "import com.ravi9a2.r4j.Metrics;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.util.StringUtils;\n" +
                "import org.springframework.stereotype.Service;\n" +
                "\n" +
                "import java.util.*;\n" +
                "\n" +
                "import java.util.concurrent.ExecutionException;\n" +
                "import org.springframework.web.reactive.function.client.WebClient;\n" +
                "\n" +
                "@Service\n" +
                "@lombok.extern.slf4j.Slf4j\n" +
                "public class ClassNameImpl implements ClassName {\n" +
                "\n", actual.toString());
    }

    @Test
    public void testOpenClass_ReactiveAndNonReactive() throws IOException {
        TypeElement classElement = Mockito.mock(TypeElement.class);
        Element element = Mockito.mock(Element.class);
        Name name = Mockito.mock(Name.class);

        Mockito.when(classElement.getEnclosingElement()).thenReturn(element);
        Mockito.when(element.toString()).thenReturn("com.ravi9a2.r4j");
        Mockito.when(classElement.getSimpleName()).thenReturn(name);
        Mockito.when(name.toString()).thenReturn("ClassName");
        HashSet<ProcessorUtility.ExecutorType> types = new HashSet<>();
        types.add(ProcessorUtility.ExecutorType.REACTIVE_MONO);
        types.add(ProcessorUtility.ExecutorType.NON_REACTIVE);
        StringBuilder actual = ProcessorUtility.openClass(classElement, types);

        Assertions.assertEquals("package com.ravi9a2.r4j;\n" +
                "\n" +
                "import com.ravi9a2.nea.core.*;\n" +
                "import com.ravi9a2.nca.*;\n" +
                "import com.ravi9a2.nca.exceptions.Status4XXException;\n" +
                "import com.ravi9a2.nca.exceptions.NetworkClientException;\n" +
                "import com.ravi9a2.nea.core.data.*;\n" +
                "import com.ravi9a2.instrumentation.enums.MetricType;\n" +
                "import com.ravi9a2.instrumentation.annotation.Instrumented;\n" +
                "import com.ravi9a2.r4j.Metrics;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.util.StringUtils;\n" +
                "import org.springframework.stereotype.Service;\n" +
                "\n" +
                "import java.util.*;\n" +
                "\n" +
                "import java.util.concurrent.ExecutionException;\n" +
                "import org.springframework.web.reactive.function.client.WebClient;\n" +
                "import org.apache.http.client.HttpClient;\n" +
                "import org.springframework.beans.factory.annotation.Qualifier;\n" +
                "import java.util.concurrent.CompletableFuture;\n" +
                "\n" +
                "@Service\n" +
                "@lombok.extern.slf4j.Slf4j\n" +
                "public class ClassNameImpl implements ClassName {\n" +
                "\n", actual.toString());
    }

    @Test
    public void testInjectDependencies_nonReactive() {

        TypeElement classElement = Mockito.mock(TypeElement.class);
        Name name = Mockito.mock(Name.class);

        Mockito.when(classElement.getSimpleName()).thenReturn(name);
        Mockito.when(name.toString()).thenReturn("ClassName");
        HashSet<ProcessorUtility.ExecutorType> types = new HashSet<>();
        types.add(ProcessorUtility.ExecutorType.NON_REACTIVE);
        StringBuilder sbActual = ProcessorUtility.injectDependencies(classElement, types);

        Assertions.assertEquals("\tprivate NonReactiveClientRegistry nonReactiveClientRegistry;\n" +
                "\tprivate NonReactiveExecutor<?> nonReactiveExecutor;\n" +
                "\n" +
                "\t@Autowired\n" +
                "\tpublic ClassNameImpl(\n" +
                "\t\t\tNonReactiveClientRegistry nonReactiveClientRegistry,\n" +
                "\t\t\tNonReactiveExecutor<?> nonReactiveExecutor) {\n" +
                "\t\tthis.nonReactiveClientRegistry = nonReactiveClientRegistry;\n" +
                "\t\tthis.nonReactiveExecutor = nonReactiveExecutor;\n" +
                "\t}\n" +
                "\n", sbActual.toString());
    }

    @Test
    public void testInjectDependencies_nonReactiveAndNonReactiveAsync() {

        TypeElement classElement = Mockito.mock(TypeElement.class);
        Name name = Mockito.mock(Name.class);

        Mockito.when(classElement.getSimpleName()).thenReturn(name);
        Mockito.when(name.toString()).thenReturn("ClassName");
        HashSet<ProcessorUtility.ExecutorType> types = new HashSet<>();
        types.add(ProcessorUtility.ExecutorType.NON_REACTIVE_ASYNC);
        StringBuilder sbActual = ProcessorUtility.injectDependencies(classElement, types);

        Assertions.assertEquals("\tprivate NonReactiveClientRegistry nonReactiveClientRegistry;\n" +
                "\tprivate NonReactiveExecutor<?> nonReactiveExecutor;\n" +
                "\tprivate ThreadPoolExecutor fallbackThreadPoolExecutor;\n" +
                "\n" +
                "\t@Autowired\n" +
                "\tpublic ClassNameImpl(\n" +
                "\t\t\tNonReactiveClientRegistry nonReactiveClientRegistry,\n" +
                "\t\t\tNonReactiveExecutor<?> nonReactiveExecutor,\n" +
                "\t\t\t@Qualifier(FALLBACK_TASK_EXECUTOR) ThreadPoolExecutor fallbackThreadPoolExecutor) {\n" +
                "\t\tthis.nonReactiveClientRegistry = nonReactiveClientRegistry;\n" +
                "\t\tthis.nonReactiveExecutor = nonReactiveExecutor;\n" +
                "\t\tthis.fallbackThreadPoolExecutor = fallbackThreadPoolExecutor;\n" +
                "\t}\n" +
                "\n", sbActual.toString());
    }

    @Test
    public void testInjectDependencies_reactive() {

        TypeElement classElement = Mockito.mock(TypeElement.class);
        Name name = Mockito.mock(Name.class);

        Mockito.when(classElement.getSimpleName()).thenReturn(name);
        Mockito.when(name.toString()).thenReturn("ClassName");
        HashSet<ProcessorUtility.ExecutorType> types = new HashSet<>();
        types.add(ProcessorUtility.ExecutorType.REACTIVE_FLUX);
        StringBuilder sbActual = ProcessorUtility.injectDependencies(classElement, types);

        Assertions.assertEquals("\tprivate ReactiveClientRegistry reactiveClientRegistry;\n" +
                "\tprivate ReactiveExecutor<?> reactiveExecutor;\n" +
                "\n" +
                "\t@Autowired\n" +
                "\tpublic ClassNameImpl(\n" +
                "\t\t\tReactiveClientRegistry reactiveClientRegistry,\n" +
                "\t\t\tReactiveExecutor<?> reactiveExecutor) {\n" +
                "\t\tthis.reactiveClientRegistry = reactiveClientRegistry;\n" +
                "\t\tthis.reactiveExecutor = reactiveExecutor;\n" +
                "\t}\n" +
                "\n", sbActual.toString());
    }

    @Test
    public void testInjectDependencies_reactiveAndNonReactive() {

        TypeElement classElement = Mockito.mock(TypeElement.class);
        Name name = Mockito.mock(Name.class);

        Mockito.when(classElement.getSimpleName()).thenReturn(name);
        Mockito.when(name.toString()).thenReturn("ClassName");
        HashSet<ProcessorUtility.ExecutorType> types = new HashSet<>();
        types.add(ProcessorUtility.ExecutorType.NON_REACTIVE);
        types.add(ProcessorUtility.ExecutorType.REACTIVE_MONO);
        StringBuilder sbActual = ProcessorUtility.injectDependencies(classElement, types);

        Assertions.assertEquals("\tprivate ReactiveClientRegistry reactiveClientRegistry;\n" +
                "\tprivate ReactiveExecutor<?> reactiveExecutor;\n" +
                "\tprivate NonReactiveClientRegistry nonReactiveClientRegistry;\n" +
                "\tprivate NonReactiveExecutor<?> nonReactiveExecutor;\n" +
                "\n" +
                "\t@Autowired\n" +
                "\tpublic ClassNameImpl(\n" +
                "\t\t\tReactiveClientRegistry reactiveClientRegistry,\n" +
                "\t\t\tReactiveExecutor<?> reactiveExecutor,\n" +
                "\t\t\tNonReactiveClientRegistry nonReactiveClientRegistry,\n" +
                "\t\t\tNonReactiveExecutor<?> nonReactiveExecutor) {\n" +
                "\t\tthis.reactiveClientRegistry = reactiveClientRegistry;\n" +
                "\t\tthis.reactiveExecutor = reactiveExecutor;\n" +
                "\t\tthis.nonReactiveClientRegistry = nonReactiveClientRegistry;\n" +
                "\t\tthis.nonReactiveExecutor = nonReactiveExecutor;\n" +
                "\t}\n" +
                "\n", sbActual.toString());
    }

    @Test
    public void testMakeNonReactiveCall_CompStage_IsSilentFalse() {
        PostCall postCall = Mockito.mock(PostCall.class);
        AnnotationWrapper annotationWrapper = AnnotationWrapper.wrap(postCall);
        Mockito.when(postCall.path()).thenReturn("/api/v2/product/aggregation");
        Mockito.when(postCall.isSilent()).thenReturn(false);
        Mockito.when(postCall.service()).thenReturn("serviceName");
        Mockito.when(postCall.circuitBreaker()).thenReturn("cbName");
        Mockito.when(postCall.bulkhead()).thenReturn("bkName");
        Mockito.when(postCall.bhEnabled()).thenReturn(true);
        Mockito.when(postCall.cbEnabled()).thenReturn(true);
        Mockito.when(postCall.fallback()).thenReturn("fallbackName");

        String actual = NonReactiveAsyncGenerator.makeCall(annotationWrapper, "fallbackMethodString", "requestString",
                "responseTypeString", "methodNameString");
        Assertions.assertEquals("\t\treturn ((NonReactiveExecutor<HttpClient>)nonReactiveExecutor)\n" +
                "\t\t\t\t.executeWithCompletionStage((NonReactiveClient<HttpClient>)(nonReactiveClientRegistry.client((\"serviceName\"))), cd)\n"
                +
                "\t\t\t\t.handleAsync((t, e) -> {\t\t\t\t\tMetrics.recordLatencyWithTags(\"path=/api/v2/product/aggregation,httpMethod=POST,method=methodNameString,ext_service=serviceName\", (System.currentTimeMillis()-startTime));\n"
                +
                "\t\t\t\t\tif (e == null) {\n" +
                "\t\t\t\t\t\treturn (responseTypeString) t;\n" +
                "\t\t\t\t\t} else if (e instanceof Status4XXException) {\n" +
                "\t\t\t\t\t\tthrow new NetworkClientException(e);\n" +
                "\t\t\t\t\t} else {\n" +
                "\t\t\t\t\t\tlog.error(\"Executing provided fallback: fallbackMethodString\");\n" +
                "\t\t\t\t\t\ttry { \n" +
                "\t\t\t\t\t\t\treturn fallbackMethodString(requestString, cd.getHttpHeaders()).get();\n" +
                "\t\t\t\t\t\t} catch (Exception ex) {\n" +
                "\t\t\t\t\t\t\tthrow new NetworkClientException(e);\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t},fallbackThreadPoolExecutor);\n", actual);
    }

    @Test
    public void testMakeNonReactiveCall_CompStage_IsSilentTrue_NoFallback() {
        PostCall postCall = Mockito.mock(PostCall.class);
        AnnotationWrapper annotationWrapper = AnnotationWrapper.wrap(postCall);
        Mockito.when(postCall.path()).thenReturn("/api/v2/product/aggregation");
        Mockito.when(postCall.isSilent()).thenReturn(true);
        Mockito.when(postCall.service()).thenReturn("serviceName");
        Mockito.when(postCall.circuitBreaker()).thenReturn("cbName");
        Mockito.when(postCall.bulkhead()).thenReturn("bkName");
        Mockito.when(postCall.bhEnabled()).thenReturn(true);
        Mockito.when(postCall.cbEnabled()).thenReturn(true);
        Mockito.when(postCall.fallback()).thenReturn("");

        String actual = NonReactiveAsyncGenerator.makeCall(annotationWrapper, "", "requestString", "responseTypeString",
                "methodNameString");
        Assertions.assertEquals("\t\treturn ((NonReactiveExecutor<HttpClient>)nonReactiveExecutor)\n" +
                "\t\t\t\t.executeWithCompletionStage((NonReactiveClient<HttpClient>)(nonReactiveClientRegistry.client((\"serviceName\"))), cd)\n"
                +
                "\t\t\t\t.handle((t, e) -> {\n" +
                "\t\t\t\t\tMetrics.recordLatencyWithTags(\"path=/api/v2/product/aggregation,httpMethod=POST,method=methodNameString,ext_service=serviceName\", (System.currentTimeMillis()-startTime));\n"
                +
                "\t\t\t\t\tif (e == null) {\n" +
                "\t\t\t\t\t\treturn (responseTypeString) t;\n" +
                "\t\t\t\t\t} else {\n" +
                "\t\t\t\t\t\treturn null;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t});\n", actual);
    }

    @Test
    public void testMakeNonReactiveCall_IsSilentFalse() {
        PostCall postCall = Mockito.mock(PostCall.class);
        AnnotationWrapper annotationWrapper = AnnotationWrapper.wrap(postCall);
        Mockito.when(postCall.path()).thenReturn("/api/v2/product/aggregation");
        Mockito.when(postCall.isSilent()).thenReturn(false);
        Mockito.when(postCall.service()).thenReturn("serviceName");
        Mockito.when(postCall.circuitBreaker()).thenReturn("cbName");
        Mockito.when(postCall.bulkhead()).thenReturn("bkName");
        Mockito.when(postCall.bhEnabled()).thenReturn(true);
        Mockito.when(postCall.cbEnabled()).thenReturn(true);
        Mockito.when(postCall.fallback()).thenReturn("fallbackName");

        String actual = NonReactiveGenerator.makeCall(annotationWrapper);
        Assertions.assertEquals("\t\ttry { \n" +
                "\t\t\treturn ((NonReactiveExecutor<HttpClient>)nonReactiveExecutor)\n" +
                "\t\t\t\t\t.execute((NonReactiveClient<HttpClient>)(nonReactiveClientRegistry.client((\"serviceName\"))), cd);\n"
                +
                "\t\t} catch (Exception ex) {\n" +
                "\t\t\tthrow ex;\n" +
                "\t\t}\n", actual);
    }

    @Test
    public void testMakeNonReactiveCall_IsSilentTrue() {
        PostCall postCall = Mockito.mock(PostCall.class);
        AnnotationWrapper annotationWrapper = AnnotationWrapper.wrap(postCall);
        Mockito.when(postCall.path()).thenReturn("/api/v2/product/aggregation");
        Mockito.when(postCall.isSilent()).thenReturn(true);
        Mockito.when(postCall.service()).thenReturn("serviceName");
        Mockito.when(postCall.circuitBreaker()).thenReturn("cbName");
        Mockito.when(postCall.bulkhead()).thenReturn("bkName");
        Mockito.when(postCall.bhEnabled()).thenReturn(true);
        Mockito.when(postCall.cbEnabled()).thenReturn(true);
        Mockito.when(postCall.fallback()).thenReturn("fallbackName");

        String actual = NonReactiveGenerator.makeCall(annotationWrapper);
        Assertions.assertEquals("\t\ttry { \n" +
                "\t\t\treturn ((NonReactiveExecutor<HttpClient>)nonReactiveExecutor)\n" +
                "\t\t\t\t\t.execute((NonReactiveClient<HttpClient>)(nonReactiveClientRegistry.client((\"serviceName\"))), cd);\n"
                +
                "\t\t} catch (Exception ex) {\n" +
                "\t\t\treturn null;\n" +
                "\t\t}\n", actual);
    }

    @Test
    public void testMakeNonReactiveCall_Empty() {
        PostCall postCall = Mockito.mock(PostCall.class);
        AnnotationWrapper annotationWrapper = AnnotationWrapper.wrap(postCall);
        Mockito.when(postCall.path()).thenReturn("/api/v2/product/aggregation");
        Mockito.when(postCall.isSilent()).thenReturn(true);
        Mockito.when(postCall.service()).thenReturn("serviceName");
        Mockito.when(postCall.circuitBreaker()).thenReturn("cbName");
        Mockito.when(postCall.bulkhead()).thenReturn("bkName");
        Mockito.when(postCall.bhEnabled()).thenReturn(true);
        Mockito.when(postCall.cbEnabled()).thenReturn(true);
        Mockito.when(postCall.fallback()).thenReturn("fallbackName");

        String actual = NonReactiveGenerator.makeCall(annotationWrapper);
        Assertions.assertEquals("\t\ttry { \n" +
                "\t\t\treturn ((NonReactiveExecutor<HttpClient>)nonReactiveExecutor)\n" +
                "\t\t\t\t\t.execute((NonReactiveClient<HttpClient>)(nonReactiveClientRegistry.client((\"serviceName\"))), cd);\n"
                +
                "\t\t} catch (Exception ex) {\n" +
                "\t\t\treturn null;\n" +
                "\t\t}\n", actual);
    }

    @Test
    public void testMakeReactiveCall_Mono() {
        PostCall postCall = Mockito.mock(PostCall.class);
        AnnotationWrapper annotationWrapper = AnnotationWrapper.wrap(postCall);
        Mockito.when(postCall.path()).thenReturn("/api/v2/product/aggregation");
        Mockito.when(postCall.isSilent()).thenReturn(true);
        Mockito.when(postCall.service()).thenReturn("serviceName");
        Mockito.when(postCall.circuitBreaker()).thenReturn("cbName");
        Mockito.when(postCall.bulkhead()).thenReturn("bkName");
        Mockito.when(postCall.bhEnabled()).thenReturn(true);
        Mockito.when(postCall.cbEnabled()).thenReturn(true);
        Mockito.when(postCall.fallback()).thenReturn("fallbackName");

        String actual = ReactiveGenerator.makeCall(annotationWrapper, ProcessorUtility.ExecutorType.REACTIVE_MONO,
                "rawReturnType");
        Assertions.assertEquals(
                "\t\tString tags = \"client=serviceName,path=/api/v2/product/aggregation,method=POST\";\n" +
                        "\t\trawReturnType val = ((ReactiveExecutor<WebClient>)reactiveExecutor)\n" +
                        "\t\t\t\t.executeToMono((ReactiveClient<WebClient>)reactiveClientRegistry.client(\"serviceName\"), cd);\n"
                        +
                        "\t\treturn com.ravi9a2.r4j.Metrics.latency(val, tags);\n",
                actual);
    }

    @Test
    public void testMakeReactiveCall_Flux() {
        PostCall postCall = Mockito.mock(PostCall.class);
        AnnotationWrapper annotationWrapper = AnnotationWrapper.wrap(postCall);
        Mockito.when(postCall.path()).thenReturn("/api/v2/product/aggregation");
        Mockito.when(postCall.isSilent()).thenReturn(true);
        Mockito.when(postCall.service()).thenReturn("serviceName");
        Mockito.when(postCall.circuitBreaker()).thenReturn("cbName");
        Mockito.when(postCall.bulkhead()).thenReturn("bkName");
        Mockito.when(postCall.bhEnabled()).thenReturn(true);
        Mockito.when(postCall.cbEnabled()).thenReturn(true);
        Mockito.when(postCall.fallback()).thenReturn("fallbackName");

        String actual = ReactiveGenerator.makeCall(annotationWrapper, ProcessorUtility.ExecutorType.REACTIVE_FLUX,
                "rawReturnType");
        Assertions.assertEquals(
                "\t\tString tags = \"client=serviceName,path=/api/v2/product/aggregation,method=POST\";\n" +
                        "\t\trawReturnType val = ((ReactiveExecutor<WebClient>)reactiveExecutor)\n" +
                        "\t\t\t\t.executeToFlux((ReactiveClient<WebClient>)reactiveClientRegistry.client(\"serviceName\"), cd);\n"
                        +
                        "\t\treturn com.ravi9a2.r4j.Metrics.latency(val, tags);\n",
                actual);
    }

    @Test
    public void testMakeReactiveCall() {
        PostCall postCall = Mockito.mock(PostCall.class);
        AnnotationWrapper annotationWrapper = AnnotationWrapper.wrap(postCall);
        Mockito.when(postCall.path()).thenReturn("/api/v2/product/aggregation");
        Mockito.when(postCall.isSilent()).thenReturn(true);
        Mockito.when(postCall.service()).thenReturn("serviceName");
        Mockito.when(postCall.circuitBreaker()).thenReturn("cbName");
        Mockito.when(postCall.bulkhead()).thenReturn("bkName");
        Mockito.when(postCall.bhEnabled()).thenReturn(true);
        Mockito.when(postCall.cbEnabled()).thenReturn(true);
        Mockito.when(postCall.fallback()).thenReturn("fallbackName");

        String actual = ReactiveGenerator.makeCall(annotationWrapper, ProcessorUtility.ExecutorType.REACTIVE_FLUX,
                "rawReturnType");
        Assertions.assertEquals(
                "\t\tString tags = \"client=serviceName,path=/api/v2/product/aggregation,method=POST\";\n" +
                        "\t\trawReturnType val = ((ReactiveExecutor<WebClient>)reactiveExecutor)\n" +
                        "\t\t\t\t.executeToFlux((ReactiveClient<WebClient>)reactiveClientRegistry.client(\"serviceName\"), cd);\n"
                        +
                        "\t\treturn com.ravi9a2.r4j.Metrics.latency(val, tags);\n",
                actual);
    }

    @Test
    public void testGenerateInstrumented() {
        ExecutableElement element = Mockito.mock(ExecutableElement.class);
        Name name = Mockito.mock(Name.class);

        PostCall postCall = Mockito.mock(PostCall.class);
        AnnotationWrapper annotationWrapper = AnnotationWrapper.wrap(postCall);
        Mockito.when(postCall.path()).thenReturn("/api/v2/product/aggregation");
        Mockito.when(postCall.isSilent()).thenReturn(true);
        Mockito.when(postCall.service()).thenReturn("serviceName");
        Mockito.when(postCall.circuitBreaker()).thenReturn("cbName");
        Mockito.when(postCall.bulkhead()).thenReturn("bkName");
        Mockito.when(postCall.bhEnabled()).thenReturn(true);
        Mockito.when(postCall.cbEnabled()).thenReturn(true);
        Mockito.when(postCall.fallback()).thenReturn("fallbackName");

        Mockito.when(element.getSimpleName()).thenReturn(name);
        Mockito.when(name.toString()).thenReturn("methodName");

        StringBuilder sbActual = new StringBuilder();
        ProcessorUtility.generateInstrumented(element, annotationWrapper, sbActual);
        Assertions.assertEquals(
                "\t@Instrumented(metricType = MetricType.HTTP, tagSet = \"path=/api/v2/product/aggregation,httpMethod=POST,method=methodName,service=serviceName\")\n",
                sbActual.toString());
    }

    @Test
    public void testGenerateMethodSignature() {
        ExecutableElement element = Mockito.mock(ExecutableElement.class);
        Name name = Mockito.mock(Name.class);
        Name paraname1 = Mockito.mock(Name.class);
        Name paraname2 = Mockito.mock(Name.class);
        TypeMirror typeMirrorReturnType = Mockito.mock(TypeMirror.class);
        TypeMirror typeMirror1 = Mockito.mock(TypeMirror.class);
        TypeMirror typeMirror2 = Mockito.mock(TypeMirror.class);

        VariableElement variableElement1 = Mockito.mock(VariableElement.class);
        VariableElement variableElement2 = Mockito.mock(VariableElement.class);
        List parameters = new ArrayList<>();
        parameters.add(variableElement1);
        parameters.add(variableElement2);

        Mockito.when(element.getSimpleName()).thenReturn(name);
        Mockito.when(name.toString()).thenReturn("methodName");
        Mockito.when(element.getParameters()).thenReturn(parameters); //

        Mockito.when(variableElement1.getSimpleName()).thenReturn(paraname1);
        Mockito.when(variableElement2.getSimpleName()).thenReturn(paraname2);
        Mockito.when(paraname1.toString()).thenReturn("paraname1");
        Mockito.when(paraname2.toString()).thenReturn("paraname2");
        Mockito.when(variableElement1.asType()).thenReturn(typeMirror1);
        Mockito.when(variableElement2.asType()).thenReturn(typeMirror2);
        Mockito.when(typeMirror1.toString()).thenReturn("type1");
        Mockito.when(typeMirror2.toString()).thenReturn("type2");
        Mockito.when(element.getReturnType()).thenReturn(typeMirrorReturnType);
        Mockito.when(typeMirrorReturnType.toString()).thenReturn("returnType");

        StringBuilder sbActual = ProcessorUtility.generateMethodSignature(element);
        Assertions.assertEquals("\tpublic returnType methodName(type1 paraname1, type2 paraname2) ",
                sbActual.toString());
    }

    @Test
    public void testGetExecuteMethodName_NonReactCompStage() {
        ExecutableElement element = Mockito.mock(ExecutableElement.class);
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getReturnType()).thenReturn(returnType);
        Mockito.when(returnType.toString()).thenReturn("java.util.concurrent.CompletableFuture<TaxonomyResponse>");
        ProcessorUtility.ExecutorType executeMethodNameActual = ProcessorUtility.getExecutorType(element, Type.HTTP);
        Assertions.assertEquals(ProcessorUtility.ExecutorType.NON_REACTIVE_ASYNC, executeMethodNameActual);
    }

    @Test
    public void testGetExecuteMethodName_NonReactive() {
        ExecutableElement element = Mockito.mock(ExecutableElement.class);
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getReturnType()).thenReturn(returnType);
        Mockito.when(returnType.toString()).thenReturn("TaxonomyResponse");
        ProcessorUtility.ExecutorType executeMethodNameActual = ProcessorUtility.getExecutorType(element, Type.HTTP);
        Assertions.assertEquals(ProcessorUtility.ExecutorType.NON_REACTIVE, executeMethodNameActual);
    }

    @Test
    public void testGetExecuteMethodName_ReactiveMono() {
        ExecutableElement element = Mockito.mock(ExecutableElement.class);
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getReturnType()).thenReturn(returnType);
        Mockito.when(returnType.toString()).thenReturn("reactor.core.publisher.Mono<TaxonomyResponse>");
        ProcessorUtility.ExecutorType executeMethodNameActual = ProcessorUtility.getExecutorType(element, Type.HTTP);
        Assertions.assertEquals(ProcessorUtility.ExecutorType.REACTIVE_MONO, executeMethodNameActual);
    }

    @Test
    public void testGetExecuteMethodName_ReactiveFlux() {
        ExecutableElement element = Mockito.mock(ExecutableElement.class);
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getReturnType()).thenReturn(returnType);
        Mockito.when(returnType.toString()).thenReturn("reactor.core.publisher.Flux<TaxonomyResponse>");
        ProcessorUtility.ExecutorType executeMethodNameActual = ProcessorUtility.getExecutorType(element, Type.HTTP);
        Assertions.assertEquals(ProcessorUtility.ExecutorType.REACTIVE_FLUX, executeMethodNameActual);
    }

    @Test
    public void testGetReturnType() {
        ExecutableElement element = Mockito.mock(ExecutableElement.class);
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getReturnType()).thenReturn(returnType);
        Mockito.when(returnType.toString()).thenReturn("reactor.core.publisher.Flux<TaxonomyResponse>");
        String actual = ProcessorUtility.getReturnType(element);
        Assertions.assertEquals("TaxonomyResponse", actual);
    }

    @Test
    public void testGetReturnType_FromFlux() {
        ExecutableElement element = Mockito.mock(ExecutableElement.class);
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getReturnType()).thenReturn(returnType);
        Mockito.when(returnType.toString()).thenReturn("reactor.core.publisher.Flux<TaxonomyResponse>");
        String actual = ProcessorUtility.getReturnType(element);
        Assertions.assertEquals("TaxonomyResponse", actual);
    }

    @Test
    public void testGetReturnType_FromMono() {
        ExecutableElement element = Mockito.mock(ExecutableElement.class);
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getReturnType()).thenReturn(returnType);
        Mockito.when(returnType.toString()).thenReturn("reactor.core.publisher.Mono<TaxonomyResponse>");
        String actual = ProcessorUtility.getReturnType(element);
        Assertions.assertEquals("TaxonomyResponse", actual);
    }

    @Test
    public void testGetReturnType_FromCompStage() {
        ExecutableElement element = Mockito.mock(ExecutableElement.class);
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getReturnType()).thenReturn(returnType);
        Mockito.when(returnType.toString()).thenReturn("java.util.concurrent.CompletableFuture<TaxonomyResponse>");
        String actual = ProcessorUtility.getReturnType(element);
        Assertions.assertEquals("TaxonomyResponse", actual);
    }

    @Test
    public void testBuildRestCallDefinition() {
        PostCall postCall = Mockito.mock(PostCall.class);
        AnnotationWrapper annotationWrapper = AnnotationWrapper.wrap(postCall);
        Mockito.when(postCall.path()).thenReturn("/api/v2/product/aggregation");
        Mockito.when(postCall.isSilent()).thenReturn(false);
        Mockito.when(postCall.service()).thenReturn("serviceName");
        Mockito.when(postCall.circuitBreaker()).thenReturn("cbName");
        Mockito.when(postCall.bulkhead()).thenReturn("bkName");
        Mockito.when(postCall.bhEnabled()).thenReturn(true);
        Mockito.when(postCall.cbEnabled()).thenReturn(true);
        Mockito.when(postCall.fallback()).thenReturn("fallbackName");

        String actual = ProcessorUtility.buildRestCallDefinition(annotationWrapper, "headersString",
                "queryParamsString", "pathParamsString", "returnTypeString", "reqObjNameString");
        Assertions.assertEquals("\t\tRestCallDefinition cd = RestCallDefinition.builder()\n" +
                "\t\t\t.isCircuitBreakerEnabled(true)\n" +
                "\t\t\t.isBulkheadEnabled(true)\n" +
                "\t\t\t.serviceTag(\"serviceName\")\n" +
                "\t\t\t.isSilentFailure(false)\n" +
                "\t\t\t.isRetryable(false)\n" +
                "\t\t\t.path(\"/api/v2/product/aggregation\")\n" +
                "\t\t\t.responseType(returnTypeString.class)\n" +
                "\t\t\t.payload(reqObjNameString)\n" +
                "\t\t\t.cbTag(\"cbName\")\n" +
                "\t\t\t.bhTag(\"bkName\")\n" +
                "\t\t\t.retryTag(\"serviceName\")\n" +
                "\t\t\t.httpHeaders(genHeadersMap)\n" +
                "\t\t\t.pathParams(genPathParamMap)\n" +
                "\t\t\t.queryParams(genQueryParamMap)\n" +
                "\t\t\t.type(Type.HTTP)\n" +
                "\t\t\t.httpMethod(HTTPMethod.POST)\n" +
                "\t\t\t.build();\n", actual);
    }

    @Test
    public void testAddLogLines() {

        String actual = ProcessorUtility.addLogLines("headersString", "queryParamsString", "pathParamsString",
                "reqObjNameString");

        Assertions.assertEquals("\t\tlog.debug(\"External call:: \" + cd.getServiceTag());\n" +
                "\t\tlog.debug(\"Request: \" + cd.getHttpMethod() + \":\" + cd.getPath());\n" +
                "\t\tlog.debug(\"Headers: \" + cd.getHttpHeaders());\n" +
                "\t\tlog.debug(\"PathParams: \" + cd.getPathParams());\n" +
                "\t\tlog.debug(\"QueryParams: \" + cd.getQueryParams());\n" +
                "\t\tlog.debug(\"Payload: \" + reqObjNameString);\n", actual);
    }

}
