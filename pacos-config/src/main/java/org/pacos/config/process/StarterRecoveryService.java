package org.pacos.config.process;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        return start("--attach");
    }

    public static boolean startInRecoveryMode() {
        return start("--recover");
    }

    private static boolean start(String mode) {
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
        command.add(mode);
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
            LOG.info("Started starter with mode '{}' (PID {}) using {}", mode, process.pid(), starterJar);
            terminateUnresponsiveParentStarter(starterJar, workingDirectory);
            return true;
        } catch (IOException | RuntimeException e) {
            LOG.error("Failed to start starter with mode '{}'", mode, e);
            return false;
        }
    }

    /**
     * If this engine was launched by the starter whose RMI endpoint just failed its health
     * check, replace that stale parent. It must be stopped forcibly: its shutdown hook would
     * otherwise stop this engine before the recovery starter has attached.
     */
    private static void terminateUnresponsiveParentStarter(Path starterJar, Path workingDirectory) {
        terminateUnresponsiveParentStarter(
                starterJar, workingDirectory, ProcessHandle.current().parent().orElse(null));
    }

    static void terminateUnresponsiveParentStarter(
            Path starterJar, Path workingDirectory, ProcessHandle parent) {
        if (parent == null || !parent.isAlive()
                || !matchesStarterProcess(parent, workingDirectory, starterJar)) {
            return;
        }

        ProcessHandle staleStarter = parent;
        LOG.warn("Starter process {} owns the unresponsive restart endpoint; replacing it", staleStarter.pid());
        staleStarter.destroyForcibly();
        try {
            staleStarter.onExit().get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            LOG.error("Stale starter process {} did not exit after forced termination", staleStarter.pid(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Interrupted while waiting for stale starter process {} to exit", staleStarter.pid(), e);
        } catch (java.util.concurrent.ExecutionException e) {
            if (staleStarter.isAlive()) {
                LOG.warn("Could not confirm termination of stale starter process {}", staleStarter.pid(), e);
            }
        }
    }

    static boolean matchesStarterProcess(ProcessHandle handle, Path workingDirectory, Path starterJar) {
        Optional<String[]> optionalArguments = handle.info().arguments();
        if (optionalArguments.isEmpty()) {
            return false;
        }

        Path expectedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
        Path expectedStarterJar = starterJar.toAbsolutePath().normalize();
        List<String> arguments = Arrays.asList(optionalArguments.get());

        boolean matchingWorkingDirectory = arguments.stream()
                .filter(argument -> argument.startsWith("-DworkingDir="))
                .map(argument -> argument.substring("-DworkingDir=".length()))
                .map(path -> resolveProcessPath(path, expectedWorkingDirectory))
                .anyMatch(expectedWorkingDirectory::equals);
        if (!matchingWorkingDirectory) {
            return false;
        }

        for (int index = 0; index + 1 < arguments.size(); index++) {
            if ("-jar".equals(arguments.get(index))) {
                Path candidateJar = resolveProcessPath(arguments.get(index + 1), expectedWorkingDirectory);
                if (expectedStarterJar.equals(candidateJar)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Path resolveProcessPath(String value, Path workingDirectory) {
        try {
            Path path = Path.of(value);
            if (!path.isAbsolute()) {
                path = workingDirectory.resolve(path);
            }
            return path.toAbsolutePath().normalize();
        } catch (RuntimeException e) {
            return null;
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
