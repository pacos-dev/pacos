package org.pacos.starter;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.pacos.config.jdbc.DataSourceLoader;
import org.pacos.config.process.StarterRecoveryService;
import org.pacos.config.property.WorkingDir;
import org.pacos.param.ArgumentBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runner is responsible for starting or attaching to a running PacOS app.
 * Pass --attach to recover supervision of an existing engine without starting another one.
 * Pass --recover to attach when possible or start the engine if no running instance can be attached.
 */
public class PacosRunner {

    private static final Logger LOG = LoggerFactory.getLogger(PacosRunner.class);
    static final String ATTACH_FLAG = "--attach";
    static final String RECOVER_FLAG = "--recover";

    public static void main(String[] args) {
        boolean attachMode = Arrays.asList(args).contains(ATTACH_FLAG);
        boolean recoveryMode = Arrays.asList(args).contains(RECOVER_FLAG);
        String[] pacosArgs = engineArguments(args);

        LOG.info("Starting PacOS Runner{}...", attachMode ? " in attach mode" : recoveryMode ? " in recovery mode" : "");
        ArgumentBinder.bindArgsToSystemProperty(pacosArgs);
        ArgumentBinder.bindJavaOptToSystemProperty(System.getenv("JAVA_OPTS"));
        configureStarterRecoveryMetadata();

        WorkingDir.initialize();
        LogbackConfig.configure(WorkingDir.load());

        DataSource dataSource = DataSourceLoader.load();

        Pacos pacos = new Pacos(List.of(pacosArgs), dataSource);
        if (attachMode || recoveryMode) {
            if (pacos.attachToRunningEngine()) {
                pacos.awaitSupervision();
                return;
            }
            if (attachMode) {
                LOG.error("Could not attach to an existing PacOS engine. No server was started.");
                return;
            }
            LOG.warn("Could not attach to an existing PacOS engine; recovery mode will start a new engine");
        }
        pacos.start();
    }

    static String[] engineArguments(String[] args) {
        return Arrays.stream(args)
                .filter(argument -> !ATTACH_FLAG.equals(argument) && !RECOVER_FLAG.equals(argument))
                .toArray(String[]::new);
    }

    static void configureStarterRecoveryMetadata() {
        if (System.getProperty(StarterRecoveryService.STARTER_JAR_PROPERTY) != null) {
            return;
        }

        try {
            var codeSource = PacosRunner.class.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                return;
            }
            Path candidate = Path.of(codeSource.getLocation().toURI()).toAbsolutePath().normalize();
            String fileName = candidate.getFileName() == null
                    ? ""
                    : candidate.getFileName().toString().toLowerCase(Locale.ROOT);
            if (Files.isRegularFile(candidate) && fileName.endsWith(".jar")) {
                System.setProperty(StarterRecoveryService.STARTER_JAR_PROPERTY, candidate.toString());
                LOG.info("Starter recovery path configured: {}", candidate);
            }
        } catch (Exception e) {
            LOG.debug("Cannot determine starter JAR path for automatic recovery", e);
        }
    }
}
