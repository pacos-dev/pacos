package org.pacos.config.property;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static access to application.properties file.
 * If a system property matches a value from {@link PropertyName}, the system property overrides the file value.
 * Set {@code pacos.environment=test} to use the test-only application properties.
 */
public class ApplicationProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationProperties.class);
    private static final String ENVIRONMENT_PROPERTY = "pacos.environment";
    private static final String TEST_ENVIRONMENT = "test";
    private static Properties properties;

    private ApplicationProperties() {
    }

    public static Properties get() {
        if (properties == null) {
            String propertyFileName = isTestEnvironment() ? "application-test.properties" : "application.properties";
            Properties loaded = new Properties();

            try (InputStream input = ApplicationProperties.class.getClassLoader().getResourceAsStream(propertyFileName)) {
                if (input == null) {
                    throw new IllegalStateException("Property file not found on the classpath: " + propertyFileName);
                }
                loaded.load(input);
            } catch (IOException e) {
                LOGGER.error("Could not load application properties from {}", propertyFileName, e);
                throw new IllegalStateException("Could not load application properties from " + propertyFileName, e);
            }

            properties = loaded;
            overrideConfigurationFromSystemProperty();
        }
        return properties;
    }

    public static Properties reloadProperties() {
        properties = null;
        return get();
    }

    private static void overrideConfigurationFromSystemProperty() {
        for (PropertyName property : PropertyName.values()) {
            String propertyName = property.getPropertyName();
            if (System.getProperties().containsKey(propertyName)) {
                properties.setProperty(propertyName, System.getProperty(propertyName));
            }
        }
    }

    private static boolean isTestEnvironment() {
        return TEST_ENVIRONMENT.equalsIgnoreCase(System.getProperty(ENVIRONMENT_PROPERTY, ""));
    }
}
