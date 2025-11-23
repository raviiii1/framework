package com.ravi9a2.r4j.aspect;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.HashSet;

public class ClassGenerator {

    private ClassGenerator() {
    }

    public static StringBuilder generateClass(TypeElement classElement) throws IOException {
        HashSet<ProcessorUtility.ExecutorType> executorTypes = new HashSet<>();
        StringBuilder allMethod = generateAllAnnotatedMethods(classElement, executorTypes);
        StringBuilder openClass = ProcessorUtility.openClass(classElement, executorTypes);
        StringBuilder dependencies = ProcessorUtility.injectDependencies(classElement, executorTypes);
        StringBuilder closeClass = ProcessorUtility.closeClass();
        return openClass.append(dependencies).append(allMethod).append(closeClass);
    }

    private static StringBuilder generateAllAnnotatedMethods(TypeElement classElement, HashSet<ProcessorUtility.ExecutorType> executorTypes) {
        StringBuilder allMethod = new StringBuilder();
        for (Element element : classElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                AnnotationWrapper wrapper = ProcessorUtility.readAnnotation(method);
                ProcessorUtility.ExecutorType executorType = ProcessorUtility.getExecutorType(method, wrapper.type());
                executorTypes.add(executorType);
                switch (executorType) {
                    case REACTIVE_MONO:
                    case REACTIVE_FLUX:
                        allMethod.append(ReactiveGenerator.generateMethod(method, wrapper, executorType));
                        break;
                    case NON_REACTIVE:
                        allMethod.append(NonReactiveGenerator.generateMethod(method, wrapper, executorType));
                        break;
                    case NON_REACTIVE_ASYNC:
                        allMethod.append(NonReactiveAsyncGenerator.generateMethod(method, wrapper, executorType));
                        break;
                    case GRPC_BLOCKING:
                    case GRPC_FUTURE:
                        allMethod.append(GRPCGenerator.generateMethod(method, wrapper, executorType));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected executor-type");
                }
            }
        }
        return allMethod;
    }
}
