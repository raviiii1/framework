package com.ravi9a2.nea.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface for annotation processing to generate the
 * implementation of an external service interface.
 *
 * @author raviprakash
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface ExternalService {
}
