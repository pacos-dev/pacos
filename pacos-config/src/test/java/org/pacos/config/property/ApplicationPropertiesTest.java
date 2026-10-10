package org.pacos.config.property;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationPropertiesTest {

    private String originalEnvironment;
    private String originalWorkingDir;
    private String originalDatasourceUrl;

    @BeforeEach
    void saveSystemProperties() {
        originalEnvironment = System.getProperty("pacos.environment");
        originalWorkingDir = System.getProperty(PropertyName.WORKING_DIR.getPropertyName());
        originalDatasourceUrl = System.getProperty("spring.datasource.url");
        System.clearProperty("spring.datasource.url");
    }

    @AfterEach
    void restoreProperties() {
        restoreSystemProperty("pacos.environment", originalEnvironment);
        restoreSystemProperty(PropertyName.WORKING_DIR.getPropertyName(), originalWorkingDir);
        restoreSystemProperty("spring.datasource.url", originalDatasourceUrl);
        ApplicationProperties.reloadProperties();
    }

    @Test
    void whenGetApplicationPropertiesThenLoadPropertyFromExplicitTestEnvironment() {
        System.setProperty("pacos.environment", "test");

        Properties properties = ApplicationProperties.reloadProperties();

        assertNotNull(properties);
        assertEquals("jdbc:hsqldb:mem:db;DB_CLOSE_DELAY=-1", properties.getProperty("spring.datasource.url"));
    }

    @Test
    void whenGetApplicationPropertiesThenLoadPropertyFromDefaultEnvironment() {
        System.clearProperty("pacos.environment");

        Properties properties = ApplicationProperties.reloadProperties();

        assertNotNull(properties);
        assertEquals("jdbc:hsqldb:${workingDir}/db/pacos.db;shutdown=true", properties.getProperty("spring.datasource.url"));
    }

    @Test
    void whenGetApplicationPropertiesThenOverrideBySystemPropertyLoadPropertyFromFile() {
        System.setProperty("pacos.environment", "test");
        System.setProperty(PropertyName.WORKING_DIR.getPropertyName(), "test_url");

        Properties properties = ApplicationProperties.reloadProperties();

        assertNotNull(properties);
        assertEquals("test_url", properties.getProperty(PropertyName.WORKING_DIR.getPropertyName()));
    }

    private static void restoreSystemProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }
}
