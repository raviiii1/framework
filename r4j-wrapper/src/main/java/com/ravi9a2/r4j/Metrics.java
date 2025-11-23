package com.ravi9a2.r4j;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Metrics {

    public static String CB_STATES_TRANSITIONS_METRIC_NAME = "CB_STATES_TRANSITIONS";
    public static String CB_FAILURE_RATE_METRIC_NAME = "CB_FAILURE_RATE";
    public static String CB_SLOW_CALL_RATE_METRIC_NAME = "CB_SLOW_CALL_RATE";
    public static String CB_NOT_PERMITTED_METRIC_NAME = "CB_CALL_NOT_PERMITTED";
    public static String BH_CALL_REJECTED_METRIC_NAME = "BH_CALL_REJECTED";
    public static String BH_AVAILABLE_PERMIT_METRIC_NAME = "BH_AVAILABLE_PERMIT";
    public static String DOWN_STREAM_LATENCY = "DOWN_STREAM_LATENCY";
    public static String TPBH_CORE_SIZE_METRIC_NAME = "TPBH_CORE_SIZE";
    public static String TPBH_CURRENT_SIZE_METRIC_NAME = "TPBH_CURRENT_SIZE";
    public static String TPBH_MAX_SIZE_METRIC_NAME = "TPBH_MAX_SIZE";
    public static String TPBH_QUEUE_DEPTH_METRIC_NAME = "TPBH_QUEUE_DEPTH";
    public static String TPBH_REMAINING_QUEUE_CAPACITY_METRIC_NAME = "TPBH_REMAINING_QUEUE_CAPACITY";
    public static String TPBH_QUEUE_CAPACITY_METRIC_NAME = "TPBH_QUEUE_CAPACITY";

    public static void increment(String key, String tags) {
    }

    public static void setGauge(String key, int amount) {
    }

    public static void setGauge(String key, int amount, String tag) {
    }

    public static void increment(String key, int value, String tags) {
    }

    public static void latency(String key, String tags, long latency) {
    }

    public static <T> Mono<T> latency(Mono<T> mono, String tags) {
        long startTime = System.currentTimeMillis();
        return mono.doOnEach(sig -> {
            if (sig.isOnComplete()) {
                Metrics.latency(DOWN_STREAM_LATENCY, tags, System.currentTimeMillis() - startTime);
            }
        });
    }

    public static <T> Flux<T> latency(Flux<T> flux, String tags) {
        long startTime = System.currentTimeMillis();
        return flux.doOnEach((sig) -> {
            if (sig.isOnComplete()) {
                latency(DOWN_STREAM_LATENCY, tags, System.currentTimeMillis() - startTime);
            }

        });
    }
}
