package com.ravi9a2.instrumentation.config;

import com.ravi9a2.instrumentation.processor.InstrumentedAspect;
import com.ravi9a2.instrumentation.processor.MetricEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration class for instrumentation library.
 * This enables AspectJ auto-proxy and provides the InstrumentedAspect bean.
 * 
 * <p>
 * The MetricEmitter bean is provided by {@link MetricEmitterConfig}.
 * To use a custom metric emitter, create a bean of type {@link MetricEmitter}
 * and it will be automatically injected into the aspect.
 * 
 * @author raviprakash
 */
@Configuration
@EnableAspectJAutoProxy
public class InstrumentationConfig {

    /**
     * Creates the InstrumentedAspect bean.
     * The MetricEmitter bean is provided by MetricEmitterConfig.
     * 
     * @param metricEmitter Metric emitter bean (autowired from MetricEmitterConfig)
     * @return InstrumentedAspect instance
     */
    @Bean
    public InstrumentedAspect instrumentedAspect(@Autowired MetricEmitter metricEmitter) {
        return new InstrumentedAspect(metricEmitter);
    }
}
