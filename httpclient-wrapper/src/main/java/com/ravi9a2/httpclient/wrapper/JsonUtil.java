package com.ravi9a2.httpclient.wrapper;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi9a2.nca.exceptions.NetworkClientException;

import java.lang.reflect.Type;

public class JsonUtil {

    private JsonUtil() {
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T parseResponse(String str, Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            return (T) parseResponse(str, clazz);
        } else {
            return null;
        }
    }

    public static <T> T parseResponse(String str, Class<T> type) {
        try {
            return mapper.readValue(str, type);
        } catch (JsonProcessingException e) {
            throw new NetworkClientException(e);
        }
    }

    public static <T> String toString(T object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new NetworkClientException(e);
        }
    }

}
