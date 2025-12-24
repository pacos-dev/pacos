package org.pacos.base.utils;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Common object mapper instance
 */
public final class ObjectMapperUtils {

    private ObjectMapperUtils() {
    }

    private static final ObjectMapper MAPPER = configure();

    private static ObjectMapper configure() {
        ObjectMapper mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        return mapper;
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
