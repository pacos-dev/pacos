package org.pacos.config.property;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Static access to application.properties file.
 * If a system property matches a value from {@link PropertyName}, the system property overrides the file value.
 * Set {@code pacos.environment=test} to use the test-only application properties.
 */
public class ApplicationProperties {

    private static final String ENVIRONMENT_PROPERTY = "pacos.environment";
    private static final String TEST_ENVIRONMENT = "test";
    private static Properties fileProperties;
    private static String loadedPropertyFileName;

    private ApplicationProperties() {
    }

    public static synchronized Properties get() {
        String propertyFileName = isTestEnvironment() ? "application-test.properties" : "application.properties";
        if (fileProperties == null || !propertyFileName.equals(loadedPropertyFileName)) {
            fileProperties = loadFileProperties(propertyFileName);
            loadedPropertyFileName = propertyFileName;
        }

        // Keep the file values as the baseline so changing/removing a system override does not leave stale values.
        Properties resolved = new Properties();
        resolved.putAll(fileProperties);
        overrideConfigurationFromSystemProperty(resolved);
        return resolved;
    }

    public static synchronized Properties reloadProperties() {
        fileProperties = null;
        loadedPropertyFileName = null;
        return get();
    }

    private static Properties loadFileProperties(String propertyFileName) {
        Properties loaded = new Properties();

        try (InputStream input = ApplicationProperties.class.getClassLoader().getResourceAsStream(propertyFileName)) {
            if (input == null) {
                throw new IllegalStateException("Property file not found on the classpath: " + propertyFileName);
            }
            loaded.load(input);
            return loaded;
        } catch (IOException e) {
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
