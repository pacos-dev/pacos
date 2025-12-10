package org.pacos.config.property;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationPropertiesTest {


    @Test
    void whenGetApplicationPropertiesThenLoadPropertyFromFile() {
        Properties properties = ApplicationProperties.reloadProperties();
        //then
        assertNotNull(properties);
        assertEquals("jdbc:hsqldb:mem:db;DB_CLOSE_DELAY=-1", properties.getProperty("spring.datasource.url"));
    }


    @Test
    void whenGetApplicationPropertiesThenOverrideBySystemPropertyLoadPropertyFromFile() {
        System.setProperty(PropertyName.WORKING_DIR.getPropertyName(), "test_url");
        Properties properties = ApplicationProperties.reloadProperties();
        //then
        assertNotNull(properties);
        assertEquals("test_url", System.getProperty(PropertyName.WORKING_DIR.getPropertyName()));

    }
}