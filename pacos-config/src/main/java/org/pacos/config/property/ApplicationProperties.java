package org.pacos.config.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Static access to application.properties file
 * If env variable contains a value from defined property in {@link PropertyName}, then value is replaced
 * <p>
 * Set the {@code pacos.environment} system property to {@code test} to use application–test.properties.
 */
public class ApplicationProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationProperties.class);
    private static final String ENVIRONMENT_PROPERTY = "pacos.environment";
    private static final String TEST_ENVIRONMENT = "test";
    private static Properties properties = null;

    private ApplicationProperties() {

    }

    public static Properties get() {
        if (ApplicationProperties.properties == null) {
            ApplicationProperties.properties = new Properties();

            String propertyFileName = isTestEnvironment() ? "application–test.properties" : "application.properties";
            try (InputStream input = ApplicationProperties.class.getClassLoader().getResourceAsStream(propertyFileName)) {
                if (input == null) {
                    throw new IllegalStateException("Property file not found: " + propertyFileName);
                }
                ApplicationProperties.properties.load(input);
                overrideConfigurationFromSystemProperty();
            } catch (Exception e) {
                LOGGER.error("Error when trying read property file from resources", e);
                ApplicationProperties.properties = new Properties();
            }
        }

        return ApplicationProperties.properties;
    }

    public static Properties reloadProperties() {
        ApplicationProperties.properties = null;
        return get();
    }

    private static void overrideConfigurationFromSystemProperty() {
        for (PropertyName property : PropertyName.values()) {
            String propertyName = property.getPropertyName();
            if (System.getProperties().containsKey(propertyName)) {
                ApplicationProperties.properties.setProperty(propertyName, System.getProperty(propertyName));
            }
        }
    }

    private static boolean isTestEnvironment() {
        return TEST_ENVIRONMENT.equalsIgnoreCase(System.getProperty(ENVIRONMENT_PROPERTY, ""));
    }
}
