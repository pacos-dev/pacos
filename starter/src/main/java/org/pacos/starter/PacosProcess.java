package org.pacos.starter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.pacos.config.jdbc.ModuleLoader;
import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.data.AppArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacosProcess {

    private static final Logger LOG = LoggerFactory.getLogger(PacosProcess.class);
    private static final long STOP_TIMEOUT_SECONDS = 10;

    private final ProcessBuilder processBuilder;
    private Process process;
    private ProcessHandle attachedProcess;

    public PacosProcess(AppArtifact engineArtifact, List<String> args) {
        this(engineArtifact, args, null);
    }

    private PacosProcess(AppArtifact engineArtifact, List<String> args, ProcessHandle attachedProcess) {
        Path jarPath = WorkingDir.getLibPath().resolve(engineArtifact.getJarPath());
        String javaHome = System.getProperty("java.home");

        List<Path> modules = ModuleLoader.load();
        this.processBuilder = ProcessCreator.build(javaHome, modules, jarPath, args);
        this.attachedProcess = attachedProcess;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook called");
            stop();
        }));
    }

    static PacosProcess attach(AppArtifact engineArtifact, List<String> args, ProcessHandle processHandle) {
        return new PacosProcess(engineArtifact, args, processHandle);
    }

    static Optional<ProcessHandle> findRunningEngine(Path workingDirectory, Path engineJar) {
        return findRunningEngine(workingDirectory, engineJar, ProcessHandle.allProcesses());
    }

    static Optional<ProcessHandle> findRunningEngine(Path workingDirectory, Path engineJar,
                                                      Stream<ProcessHandle> candidates) {
        Path expectedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
        Path expectedEngineJar = engineJar.toAbsolutePath().normalize();
        return candidates.filter(ProcessHandle::isAlive)
                .filter(handle -> isMatchingEngine(handle, expectedWorkingDirectory, expectedEngineJar))
                .findFirst();
    }

    private static boolean isMatchingEngine(ProcessHandle handle, Path expectedWorkingDirectory, Path expectedEngineJar) {
        Optional<String[]> optionalArguments = handle.info().arguments();
        if (optionalArguments.isEmpty()) {
            return false;
        }

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
                if (expectedEngineJar.equals(candidateJar)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Path resolveProcessPath(String value, Path workingDirectory) {
        Path path = Path.of(value);
        if (!path.isAbsolute()) {
            path = workingDirectory.resolve(path);
        }
        return path.toAbsolutePath().normalize();
    }

    boolean isAttached() {
        return attachedProcess != null;
    }

    public void run() {
        LOG.info("Starting PacOS engine...");
        try {
            this.process = startProcess(processBuilder);
            printProcessStreams(process);
            process.waitFor();
        } catch (IOException e) {
            LOG.warn("PacOS engine stopped", e);
        } catch (InterruptedException e) {
            if (process != null) {
                process.destroyForcibly();
            }
            Thread.currentThread().interrupt();
            LOG.warn("Interrupted while waiting for PacOS engine", e);
        }
    }

    static Process startProcess(ProcessBuilder processBuilder) throws IOException {
        return processBuilder.start();
    }

    /**
     * Drain both pipes concurrently. Reading stdout to EOF before reading stderr can deadlock
     * when the child process fills its stderr pipe while the parent waits on stdout.
     */
    static void printProcessStreams(Process process) throws InterruptedException {
        Thread stdoutReader = new Thread(() -> printStream(process.getInputStream(), false), "pacos-engine-stdout");
        Thread stderrReader = new Thread(() -> printStream(process.getErrorStream(), true), "pacos-engine-stderr");

        stdoutReader.start();
        stderrReader.start();

        stdoutReader.join();
        stderrReader.join();
    }

    public void stop() {
        stopAndAwait();
    }

    /**
     * Stops the engine and waits until termination is confirmed before a restart can begin.
     *
     * @return true when the child process is no longer running
     */
    boolean stopAndAwait() {
        if (attachedProcess != null) {
            return stopAttachedProcess();
        }
        if (process == null) {
            LOG.info("Can't stop engine. Process is null");
            return true;
        }
        LOG.info("Stopping engine... waiting up to {} seconds before force termination...", STOP_TIMEOUT_SECONDS);
        try {
            terminateProcessWithTimeout(process, STOP_TIMEOUT_SECONDS);
            boolean stopped = !process.isAlive();
            if (stopped) {
                LOG.info("Engine process stopped with exit code: {}", process.exitValue());
            } else {
                LOG.error("Engine process is still running; restart will be aborted");
            }
            return stopped;
        } catch (RuntimeException e) {
            if (process.isAlive()) {
                LOG.error("Error while stopping engine. Automatic restart cannot safely continue.", e);
            }
            return !process.isAlive();
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while stopping engine. Automatic restart cannot safely continue.", e);
            return false;
        }
    }

    private boolean stopAttachedProcess() {
        ProcessHandle existingProcess = attachedProcess;
        if (!existingProcess.isAlive()) {
            attachedProcess = null;
            return true;
        }

        LOG.info("Stopping attached PacOS engine process {}...", existingProcess.pid());
        try {
            existingProcess.destroy();
            if (!awaitExit(existingProcess, STOP_TIMEOUT_SECONDS)) {
                LOG.warn("Attached engine did not stop gracefully; requesting forced termination");
                existingProcess.destroyForcibly();
                if (!awaitExit(existingProcess, STOP_TIMEOUT_SECONDS)) {
                    LOG.error("Attached engine process {} is still running; restart will be aborted",
                            existingProcess.pid());
                    return false;
                }
            }
            attachedProcess = null;
            LOG.info("Attached engine process stopped");
            return true;
        } catch (InterruptedException e) {
            existingProcess.destroyForcibly();
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while stopping attached engine; restart will be aborted", e);
            return false;
        }
    }

    private static boolean awaitExit(ProcessHandle processHandle, long timeoutSeconds) throws InterruptedException {
        try {
            processHandle.onExit().get(timeoutSeconds, TimeUnit.SECONDS);
            return !processHandle.isAlive();
        } catch (TimeoutException e) {
            return false;
        } catch (ExecutionException e) {
            return !processHandle.isAlive();
        }
    }

    static void terminateProcessWithTimeout(Process process, long timeoutSeconds) throws InterruptedException {
        LOG.info("Stopping the process...");
        process.destroy();

        if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            LOG.info("Process did not exit after {} seconds. Force exit...", timeoutSeconds);
            process.destroyForcibly();
            // Wait synchronously: callers must not start a replacement process until this
            // process has actually exited.
            process.waitFor();
        }
    }

    private static void printStream(InputStream inputStream, boolean errorStream) {
        if (inputStream == null) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (errorStream) {
                    LOG.warn(line);
                } else {
                    LOG.info(line);
                }
            }
        } catch (IOException e) {
            LOG.warn("Error while reading PacOS engine output stream");
        }
    }
}