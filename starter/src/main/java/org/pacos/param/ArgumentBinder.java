package org.pacos.param;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArgumentBinder {

    private static final Logger LOG = LoggerFactory.getLogger(ArgumentBinder.class);

    private ArgumentBinder() {

    }

    public static void bindArgsToSystemProperty(String[] args) {
        for (String param : args) {
            if (param.contains("=") && param.split("=").length > 1) {
                String propertyName = param.split("=")[0].trim();
                String propertyValue = param.split("=")[1].trim();
                if (propertyName.startsWith("-D")) {
                    propertyName = propertyName.substring(2);
                }
                System.setProperty(propertyName, propertyValue);
                LOG.info("Bind argument: '{}':'{}'", propertyName, propertyValue);
            }
        }
    }

    public static void bindJavaOptToSystemProperty(String javaOpts) {
        if (javaOpts != null) {
            LOG.info("Passed javaOpts={}", javaOpts);
            if (javaOpts.startsWith("\"") && javaOpts.endsWith("\"")) {
                javaOpts = javaOpts.substring(1, javaOpts.length() - 1);
            }
            String[] optsArray = javaOpts.split("\\s+");
            bindArgsToSystemProperty(optsArray);
        }
    }
}
