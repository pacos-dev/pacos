package org.pacos.base.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ObjectMapperUtilsTest {

    @Test
    void whenGetMapperThenReturnDefaultMapper() {
        assertNotNull(ObjectMapperUtils.getMapper());
    }
}