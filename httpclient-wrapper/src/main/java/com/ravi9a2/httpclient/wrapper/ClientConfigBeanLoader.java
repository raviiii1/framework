package com.ravi9a2.httpclient.wrapper;

import com.ravi9a2.nca.NonReactiveClient;
import com.ravi9a2.nca.NonReactiveClientRegistry;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class ClientConfigBeanLoader {

    private static final String DEFAULT = "default";

    @Bean("clientConfigs")
    @ConfigurationProperties(prefix = "downstream")
    public Map<String, Map<String, String>> webClientConfigs() {
        return new HashMap<>();
    }

    @Bean
    public NonReactiveClientRegistry clientConfigRegistry(@Qualifier("clientConfigs") Map<String, Map<String, String>> clientConfigs) {

        Map<String, NonReactiveClient<HttpClient>> allClients = clientConfigs.entrySet().stream()
                .filter(e -> Objects.nonNull(e.getValue()))
                .filter(e -> !(((String) new ArrayList(((LinkedHashMap) e.getValue()).keySet()).get(0)).split("\\.").length > 1))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> constructClientConfig(e.getKey(), e.getValue(), clientConfigs.get(DEFAULT))));
        return NonReactiveClientRegistry.of(allClients);
    }

    private HttpClientWrapper constructClientConfig(String name, Map<String, String> c, Map<String, String> d) {
        return new HttpClientWrapperBuilder()
                .clientName(name)
                .baseUrl(c.get("baseUrl"))
                .writeTimeout(Integer.parseInt(getValue(c, d, "writeTimeout", "10000")))
                .readTimeout(Integer.parseInt(getValue(c, d, "readTimeout", "10000")))
                .maxConnections(Integer.parseInt(getValue(c, d, "maxConnections", "100")))
                .connectTimeout(Integer.parseInt(getValue(c, d, "connectTimeout", "10000")))
                .socketTimeout(Integer.parseInt(getValue(c, d, "socketTimeout", "0")))
                .defaultMaxPerRoute(Integer.parseInt(getValue(c,d,"defaultMaxPerRoute", "2")))
                .header(c.get("authKey"), c.get("authSecret"))
                .header(c.get("secondAuthKey"), c.get("secondAuthSecret"))
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
