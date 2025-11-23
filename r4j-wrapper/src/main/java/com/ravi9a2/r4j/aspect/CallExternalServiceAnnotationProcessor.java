package com.ravi9a2.r4j.aspect;

import com.google.auto.service.AutoService;
import com.ravi9a2.nea.annotations.ExternalService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

/**
 * Annotation Processor that interprets the annotations provided by the
 * `ravi9a2.networking.api` and generates the R4J specific implementation
 * of the methods annotated with `@Call`, `@PostCall`, `@GetCall`,
 * `@DeleteCall`,
 * `@PatchCall`, and `@PutCall` inside the interface annotated with
 * `@ExternalService`.
 *
 * @author raviprakash
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.ravi9a2.nea.annotations.ExternalService")
public class CallExternalServiceAnnotationProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (Element ele : roundEnv.getElementsAnnotatedWith(ExternalService.class)) {
            if (ele.getKind() == ElementKind.INTERFACE) {
                TypeElement classElement = (TypeElement) ele;
                try {
                    JavaFileObject file = processingEnv.getFiler()
                            .createSourceFile(classElement.getQualifiedName() + "Impl");
                    Writer writer = file.openWriter();
                    StringBuilder generatedClass = ClassGenerator.generateClass(classElement);
                    writer.append(generatedClass);
                    writer.flush();
                    writer.close();
                } catch (final IOException ex) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.getMessage());
                }
            }
        }
        return true;
    }
}
