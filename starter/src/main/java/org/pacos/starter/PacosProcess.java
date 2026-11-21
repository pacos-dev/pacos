package org.pacos.starter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
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

    static Optional<ProcessHandle> findRunningEngine(Path workingDirectory, Path engineJar,
                                                      Stream<ProcessHandle> candidates) {
        return RunningEngineLocator.find(workingDirectory, engineJar, candidates);
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

        Thread[] streamReaders = startProcessStreamReaders(child);
        try {
            int exitCode = child.waitFor();
            closeProcessStreams(child);
            awaitStreamReaders(streamReaders);
            LOG.info("PacOS engine process exited with code {}", exitCode);
            return exitCode;
        } catch (InterruptedException e) {
            child.destroyForcibly();
            closeProcessStreams(child);
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
        awaitStreamReaders(startProcessStreamReaders(process));
    }

    private static Thread[] startProcessStreamReaders(Process process) {
        Thread stdoutReader = new Thread(() -> printStream(process.getInputStream(), false), "pacos-engine-stdout");
        Thread stderrReader = new Thread(() -> printStream(process.getErrorStream(), true), "pacos-engine-stderr");
        stdoutReader.setDaemon(true);
        stderrReader.setDaemon(true);
        stdoutReader.start();
        stderrReader.start();
        return new Thread[] { stdoutReader, stderrReader };
    }

    private static void awaitStreamReaders(Thread[] readers) throws InterruptedException {
        for (Thread reader : readers) {
            reader.join(TimeUnit.SECONDS.toMillis(1));
            if (reader.isAlive()) {
                LOG.warn("PacOS engine output reader {} did not stop after process termination", reader.getName());
            }
        }
    }

    private static void closeProcessStreams(Process process) {
        closeQuietly(process.getInputStream());
        closeQuietly(process.getErrorStream());
        closeQuietly(process.getOutputStream());
    }

    private static void closeQuietly(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            LOG.debug("Could not close PacOS engine process stream", e);
        }
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
        LOG.warn("Force-stopping PacOS engine process {} for restart", child.pid());
        try {
            return forceTerminateProcessAndAwait(child, STOP_TIMEOUT_SECONDS);
        } catch (InterruptedException e) {
            child.destroyForcibly();
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while force-stopping engine; automatic restart cannot safely continue.", e);
            return false;
        }
    }

    private boolean stopAttachedProcess() {
        ProcessHandle existingProcess = attachedProcess;
        if (!existingProcess.isAlive()) {
            return true;
        }

        LOG.warn("Force-stopping attached PacOS engine process {} for restart", existingProcess.pid());
        try {
            existingProcess.destroyForcibly();
            if (!awaitExit(existingProcess, STOP_TIMEOUT_SECONDS)) {
                LOG.error("Attached engine process {} is still running after forced termination; restart will be aborted",
                        existingProcess.pid());
                return false;
            }
            LOG.info("Attached PacOS engine process stopped");
            return true;
        } catch (InterruptedException e) {
            existingProcess.destroyForcibly();
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while force-stopping attached engine; restart will be aborted", e);
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

    static boolean forceTerminateProcessAndAwait(Process process, long timeoutSeconds)
            throws InterruptedException {
        LOG.info("Sending forced termination to process {}...", process.pid());
        process.destroyForcibly();
        boolean stopped = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!stopped) {
            LOG.error("Process {} is still running after forced termination", process.pid());
        }
        return stopped || !process.isAlive();
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
            LOG.warn("Error while reading PacOS engine output stream", e);
        }
    }
}
