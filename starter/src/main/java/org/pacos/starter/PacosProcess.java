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
    private final Object lifecycleLock = new Object();
    private final ProcessHandle attachedProcess;
    private volatile boolean stopRequested;
    private Process process;

    public PacosProcess(AppArtifact engineArtifact, List<String> args) {
        this(engineArtifact, args, null);
    }

    private PacosProcess(AppArtifact engineArtifact, List<String> args, ProcessHandle attachedProcess) {
        Path jarPath = WorkingDir.getLibPath().resolve(engineArtifact.getJarPath());
        String javaHome = System.getProperty("java.home");

        List<Path> modules = ModuleLoader.load();
        this.processBuilder = ProcessCreator.build(javaHome, modules, jarPath, args);
        this.attachedProcess = attachedProcess;
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

    /**
     * Runs this process once. Pacos owns the supervision loop and decides whether an exit
     * was requested or unexpected; keeping that policy outside this wrapper prevents a
     * stopped process from racing with a second replacement process.
     *
     * @return the engine exit code, or -1 if the process could not be started/waited for
     */
    public int run() {
        if (attachedProcess != null) {
            return awaitAttachedProcess();
        }

        Process child;
        synchronized (lifecycleLock) {
            if (stopRequested) {
                return 0;
            }
            LOG.info("Starting PacOS engine...");
            try {
                process = startProcess(processBuilder);
                child = process;
            } catch (IOException e) {
                LOG.error("Could not start PacOS engine process", e);
                return -1;
            }
        }

        try {
            printProcessStreams(child);
            int exitCode = child.waitFor();
            LOG.info("PacOS engine process exited with code {}", exitCode);
            return exitCode;
        } catch (InterruptedException e) {
            child.destroyForcibly();
            Thread.currentThread().interrupt();
            LOG.warn("Interrupted while waiting for PacOS engine", e);
            return -1;
        }
    }

    private int awaitAttachedProcess() {
        try {
            attachedProcess.onExit().get();
            LOG.info("Attached PacOS engine process {} exited", attachedProcess.pid());
            return 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Interrupted while waiting for attached PacOS engine process {}", attachedProcess.pid(), e);
            return -1;
        } catch (ExecutionException e) {
            LOG.warn("Could not observe the exit of attached PacOS engine process {}", attachedProcess.pid(), e);
            return attachedProcess.isAlive() ? -1 : 0;
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
        Process child;
        synchronized (lifecycleLock) {
            stopRequested = true;
            child = process;
        }

        if (attachedProcess != null) {
            return stopAttachedProcess();
        }
        if (child == null || !child.isAlive()) {
            LOG.info("Engine process is already stopped or has not started");
            return true;
        }
        LOG.info("Stopping engine... waiting up to {} seconds before force termination...", STOP_TIMEOUT_SECONDS);
        try {
            terminateProcessWithTimeout(child, STOP_TIMEOUT_SECONDS);
            boolean stopped = !child.isAlive();
            if (stopped) {
                LOG.info("Engine process stopped with exit code: {}", child.exitValue());
            } else {
                LOG.error("Engine process is still running; restart will be aborted");
            }
            return stopped;
        } catch (RuntimeException e) {
            if (child.isAlive()) {
                LOG.error("Error while stopping engine. Automatic restart cannot safely continue.", e);
            }
            return !child.isAlive();
        } catch (InterruptedException e) {
            child.destroyForcibly();
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while stopping engine. Automatic restart cannot safely continue.", e);
            return false;
        }
    }

    private boolean stopAttachedProcess() {
        ProcessHandle existingProcess = attachedProcess;
        if (!existingProcess.isAlive()) {
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
