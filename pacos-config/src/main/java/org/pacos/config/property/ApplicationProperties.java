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
    private static Properties fileProperties;

    private ApplicationProperties() {
    }

    public static synchronized Properties get() {
        if (fileProperties == null) {
            fileProperties = loadFileProperties();
        }

        // Keep the file values as the baseline so changing/removing a system override does not leave stale values.
        Properties resolved = new Properties();
        resolved.putAll(fileProperties);
        overrideConfigurationFromSystemProperty(resolved);
        return resolved;
    }

    public static synchronized Properties reloadProperties() {
        fileProperties = null;
        return get();
    }

    private static Properties loadFileProperties() {
        String propertyFileName = isTestEnvironment() ? "application-test.properties" : "application.properties";
        Properties loaded = new Properties();

        try (InputStream input = ApplicationProperties.class.getClassLoader().getResourceAsStream(propertyFileName)) {
            if (input == null) {
                throw new IllegalStateException("Property file not found on the classpath: " + propertyFileName);
            }
            loaded.load(input);
            return loaded;
        } catch (IOException e) {
            LOGGER.error("Could not load application properties from {}", propertyFileName, e);
            throw new IllegalStateException("Could not load application properties from " + propertyFileName, e);
        }
    }

    private static void overrideConfigurationFromSystemProperty(Properties target) {
        for (PropertyName property : PropertyName.values()) {
            String propertyName = property.getPropertyName();
            if (System.getProperties().containsKey(propertyName)) {
                target.setProperty(propertyName, System.getProperty(propertyName));
            }
        }
    }

    private static boolean isTestEnvironment() {
        return TEST_ENVIRONMENT.equalsIgnoreCase(System.getProperty(ENVIRONMENT_PROPERTY, ""));
    }
}
