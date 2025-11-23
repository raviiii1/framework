package com.ravi9a2.r4j.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class CBAdminControls {

    private static final Logger log = Logger.getLogger(CBAdminControls.class.getName());
    private static final String FORCE_OPEN_CB = "FORCE_OPEN_CB";
    private static final String FORCE_CLOSE_CB = "FORCE_CLOSE_CB";
    private static final String RESET_CB = "RESET_CB";
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    public CBAdminControls(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public void override(String serviceName, String state) {
        switch (state) {
            case FORCE_OPEN_CB:
                forceOpenCB(serviceName);
                break;
            case FORCE_CLOSE_CB:
                forceCloseCB(serviceName);
                break;
            case RESET_CB:
                reset(serviceName);
                break;
            default:
                log.warning("Invalid state: " + state + " for service: " + serviceName);
        }
    }

    private void forceCloseCB(String serviceName) {
        circuitBreakerRegistry.circuitBreaker(serviceName).transitionToDisabledState();
    }

    private void forceOpenCB(String serviceName) {
        circuitBreakerRegistry.circuitBreaker(serviceName).transitionToForcedOpenState();
    }

    private void reset(String serviceName) {
        if ("all" .equals(serviceName)) {
            circuitBreakerRegistry.getAllCircuitBreakers().toStream().forEach(CircuitBreaker::reset);
        } else {
            circuitBreakerRegistry.circuitBreaker(serviceName).reset();
        }
    }
}
