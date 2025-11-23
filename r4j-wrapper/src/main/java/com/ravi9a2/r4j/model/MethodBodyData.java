package com.ravi9a2.r4j.model;

import com.ravi9a2.r4j.aspect.AnnotationWrapper;
import com.ravi9a2.r4j.aspect.ProcessorUtility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodBodyData {
    String headers;
    String queryParams;
    String pathParams;
    String fallbackMethod;
    AnnotationWrapper callAnnot;
    ProcessorUtility.ExecutorType executorType;
    String reactiveCallString;
    String nonReactiveCallString;
    String returnType;
    String requestObjectName;
}
