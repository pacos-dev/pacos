package org.pacos.base.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Common object mapper instance
 */
public final class ObjectMapperUtils {

    private ObjectMapperUtils() {
    }

    private static final ObjectMapper MAPPER = configure();

    private static ObjectMapper configure() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
