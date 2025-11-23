package com.ravi9a2.instrumentation.enums;

/**
 * Enumeration of metric types that can be used for instrumentation.
 * 
 * @author raviprakash
 */
public enum MetricType {
    /**
     * HTTP/REST API call metrics
     */
    HTTP,

    /**
     * RPC call metrics
     */
    RPC,

    /**
     * Database operation metrics
     */
    DATABASE,

    /**
     * Cache operation metrics
     */
    CACHE,

    /**
     * Generic metrics
     */
    GENERIC
}
