package com.ravi9a2.webclient.wrapper;

import com.ravi9a2.nca.ReactiveClient;
import com.ravi9a2.nca.ReactiveClientRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
public class WebClientWrapperBeanLoader {

    private static final String DEFAULT = "default";

    @Value("${webClient.tp.enableSeparateTP:false}")
    boolean webClientEnableSeparateTP;

    @Value("${webClient.tp.corePoolSize:1}")
    Integer webClientTPCorePoolSize;

    @Value("${webClient.tp.maxPoolSize:200}")
    Integer webClientTPMaxPoolSize;

    @Value("${webClient.tp.queueCapacity:65556}")
    Integer webClientTPQueueCapacity;

    @Value("${webClient.tp.threadNamePrefix:WebClient-TP-}")
    String webClientTPThreadNamePrefix;

    @Value("${webClient.codec.inMemoryBufferSizeInKB:256}")
    int webClientCodecInMemoryBufferSizeInKB;

    @Bean
    public ThreadPoolTaskExecutor webClientTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(webClientTPCorePoolSize);
        executor.setMaxPoolSize(webClientTPMaxPoolSize);
        executor.setQueueCapacity(webClientTPQueueCapacity);
        executor.setThreadNamePrefix(webClientTPThreadNamePrefix);
        executor.initialize();
        return executor;
    }

    @Bean("webClientConfigs")
    @ConfigurationProperties(prefix = "downstream")
    public Map<String, Map<String, String>> webClientConfigs() {
        return new HashMap<>();
    }

    @Bean
    public ReactiveClientRegistry reactiveWebClientRegistry(@Qualifier("webClientConfigs") Map<String, Map<String, String>> clientConfigs) {

        Map<String, ReactiveClient<WebClient>> allClients = clientConfigs.entrySet().stream()
                .filter(e -> Objects.nonNull(e.getValue()))
                .filter(e -> !(((String) new ArrayList(((LinkedHashMap) e.getValue()).keySet()).get(0)).split("\\.").length > 1))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> constructWebClientWrapper(e.getKey(), e.getValue(), clientConfigs.get(DEFAULT))));
        return ReactiveClientRegistry.of(allClients);
    }

    private WebClientWrapper constructWebClientWrapper(String name, Map<String, String> c, Map<String, String> d) {
        return new WebClientWrapperBuilder()
                .clientName(name)
                .baseUrl(c.get("baseUrl"))
                .connectTimeout(Integer.parseInt(getValue(c, d, "connectTimeout", "10000")))
                .maxConnections(Integer.parseInt(getValue(c, d, "maxConnections", "100")))
                .readTimeout(Integer.parseInt(getValue(c, d, "readTimeout", "10000")))
                .writeTimeout(Integer.parseInt(getValue(c, d, "writeTimeout", "10000")))
                .header("Content-Type", "application/json")
                .header(c.get("authKey"), c.get("authSecret"))
                .header(c.get("secondAuthKey"), c.get("secondAuthSecret"))
                .inMemoryBufferSizeInKB(webClientCodecInMemoryBufferSizeInKB)
                .webClientTaskExecutor(webClientEnableSeparateTP ? webClientTaskExecutor() : null)
                .build();
    }

    private String getValue(Map<String, String> c, Map<String, String> d, String k, String v) {
        if (Objects.isNull(d))
            d = Collections.emptyMap();
        if (Objects.isNull(c))
            c = Collections.emptyMap();
        return c.getOrDefault(k, d.getOrDefault(k, v));
    }

}