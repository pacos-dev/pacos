package org.pacos.config.process;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.pacos.config.property.WorkingDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StarterRecoveryService {
    public static final String STARTER_JAR_PROPERTY = "pacos.starter.jar";
    public static final String STARTER_ARGS_PROPERTY = "pacos.starter.args";

    private static final Logger LOG = LoggerFactory.getLogger(StarterRecoveryService.class);

    private StarterRecoveryService() {
    }

    public static boolean startInAttachMode() {
        String configuredJar = System.getProperty(STARTER_JAR_PROPERTY);
        if (configuredJar == null || configuredJar.isBlank()) {
            LOG.warn("Starter recovery is unavailable because the starter JAR path was not propagated to PacOS");
            return false;
        }

        Path starterJar;
        try {
            starterJar = Path.of(configuredJar).toAbsolutePath().normalize();
        } catch (RuntimeException e) {
            LOG.warn("Invalid starter JAR path '{}'; cannot recover the starter", configuredJar, e);
            return false;
        }
        if (!Files.isRegularFile(starterJar)) {
            LOG.warn("Starter JAR '{}' does not exist; cannot recover the starter", starterJar);
            return false;
        }

        Path workingDirectory = WorkingDir.load().toAbsolutePath().normalize();
        List<String> command = new ArrayList<>();
        command.add(javaExecutable().toString());
        String javaOpts = System.getenv("JAVA_OPTS");
        if (javaOpts != null && !javaOpts.isBlank()) {
            command.addAll(Arrays.asList(javaOpts.trim().split("\\s+")));
        }
        command.add("-jar");
        command.add(starterJar.toString());
        command.add("--attach");
        command.addAll(readArguments());
        command.add("-DworkingDir=" + workingDirectory);

        Path logFile = workingDirectory.resolve("logs").resolve("starter-recovery.log");
        try {
            Files.createDirectories(logFile.getParent());
            Process process = new ProcessBuilder(command)
                    .directory(workingDirectory.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()))
                    .start();
            LOG.info("Started starter recovery in attach mode (PID {}) using {}", process.pid(), starterJar);
            return true;
        } catch (IOException | RuntimeException e) {
            LOG.error("Failed to start starter recovery in attach mode", e);
            return false;
        }
    }

    static List<String> readArguments() {
        String encodedArguments = System.getProperty(STARTER_ARGS_PROPERTY);
        if (encodedArguments == null || encodedArguments.isBlank()) {
            return List.of();
        }

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(encodedArguments), StandardCharsets.UTF_8);
            if (decoded.isEmpty()) {
                return List.of();
            }
            return List.of(decoded.split(String.valueOf((char) 0), -1));
        } catch (IllegalArgumentException e) {
            LOG.warn("Starter recovery arguments are invalid; using no forwarded arguments", e);
            return List.of();
        }
    }

    private static Path javaExecutable() {
        String executable = System.getProperty("os.name", "").toLowerCase().contains("win") ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", executable);
    }
}
