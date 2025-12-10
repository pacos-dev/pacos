package org.pacos.config.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Static access to application.properties file
 * If env variable contains a value from defined property in {@link PropertyName}, then value is replaced
 * <p>
 * For test environment application-test.properties file is used
 */
public class ApplicationProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationProperties.class);
    private static Properties properties = null;

    private ApplicationProperties() {

    }

    public static Properties get() {
        if (ApplicationProperties.properties == null) {
            ApplicationProperties.properties = new Properties();

            try {
                String propertyFileName = "application.properties";
                if (isTestEnvironment()) {
                    propertyFileName = "application–test.properties";
                }
                InputStream input = ApplicationProperties.class.getClassLoader().getResourceAsStream(propertyFileName);
                ApplicationProperties.properties.load(input);

                overrideConfigurationFromSystemProperty();

            } catch (Exception e) {
                LOGGER.error("Error when trying read property file from resources", e);
            }
        }

        return ApplicationProperties.properties;
    }

    public static Properties reloadProperties(){
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
        return Stream.of(Thread.currentThread().getStackTrace())
                .anyMatch(e -> e.getClassName().contains("TestMethodTestDescriptor") || e.getClassName()
                        .contains("TestContextManager"));
    }

}
