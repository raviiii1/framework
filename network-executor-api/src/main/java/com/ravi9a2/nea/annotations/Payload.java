package com.ravi9a2.nea.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Payload of a Call
 *
 * @author raviprakash
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface Payload {
}
