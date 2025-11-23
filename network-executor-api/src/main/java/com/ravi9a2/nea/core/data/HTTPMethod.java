package com.ravi9a2.nea.core.data;

import java.util.Arrays;

/**
 * Enum for HTTP methods.
 *
 * @author raviprakash
 */
public enum HTTPMethod {
    GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE"), PATCH("PATCH");

    String value;

    HTTPMethod(String name) {
        this.value = name;
    }

    public String value() {
        return value;
    }

    /**
     * Returns the HTTPMethod for an equivalent string.
     *
     * @param name String name if the HTTPMethod
     * @return HTTPMethod
     */
    public HTTPMethod of(String name) {
        return Arrays.stream(HTTPMethod.values())
                .filter(m -> m.value.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(name));
    }
}
