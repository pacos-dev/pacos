package org.pacos.starter;

import org.pacos.config.jdbc.ModuleLoader;
import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.data.AppArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PacosProcess {

    private static final Logger LOG = LoggerFactory.getLogger(PacosProcess.class);

    private final ProcessBuilder processBuilder;
    private Process process;

    public PacosProcess(AppArtifact engineArtifact, List<String> args) {
        Path jarPath = WorkingDir.getLibPath().resolve(engineArtifact.getJarPath());
        String javaHome = System.getProperty("java.home");

        List<Path> modules = ModuleLoader.load();
        this.processBuilder = ProcessCreator.build(javaHome, modules, jarPath, args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook called");
            stop();
        }));
    }

    public void run() {
        LOG.info("Starting Coupler engine...");
        try {
            this.process = startProcess(processBuilder);
            printProcessStreams(process);
            process.waitFor();
        } catch (IOException e) {
            LOG.warn("Coupler engine stopped", e);
        } catch (InterruptedException e) {
            if (process != null) {
                process.destroyForcibly();
            }
            Thread.currentThread().interrupt();
            LOG.warn("Interrupted while waiting for Coupler engine", e);
        }
    }

    static Process startProcess(ProcessBuilder processBuilder) throws IOException {
        return processBuilder.start();
    }

    static void printProcessStreams(Process process) throws InterruptedException {
        Thread stdoutReader = new Thread(() -> printStream(process.getInputStream()), "pacos-engine-stdout");
        Thread stderrReader = new Thread(() -> printStream(process.getErrorStream()), "pacos-engine-stderr");

        stdoutReader.start();
        stderrReader.start();

        stdoutReader.join();
        stderrReader.join();
    }

    public void stop() {
        if (process == null) {
            LOG.info("Can't stop engine. Process is null");
            return;
        }
        long timeout = 10;
        LOG.info("Stopping engine.... wait max {} seconds...", timeout);
        try {
            terminateProcessWithTimeout(process, timeout);
            LOG.info("Terminated process finish");
        } catch (RuntimeException e) {
            if (process.isAlive()) {
                LOG.error("Error while stopping engine. Automatic restart failed! ", e);
            }
        } catch (InterruptedException e) {
            LOG.error("Interrupted while stopping engine. Automatic restart failed! ", e);
            Thread.currentThread().interrupt();
        }
    }

    private static void terminateProcessWithTimeout(Process process, long timeoutSeconds) throws InterruptedException {
        Thread terminationThread = new Thread(() -> {
            LOG.info("Stopping the process...");
            process.destroy();

            try {
                if (process.isAlive() && !process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                    LOG.info("Process did not exit after {} seconds. Force exit...", timeoutSeconds);
                    process.destroyForcibly();
                }
                if (!process.isAlive()) {
                    LOG.info("Process destroyed with exit code: {}", process.exitValue());
                }

            } catch (InterruptedException e) {
                LOG.warn("Terminate process interrupted", e);
                Thread.currentThread().interrupt();
            }

        });

        terminationThread.start();
        terminationThread.join(timeoutSeconds * 1000);
    }

    private static void printStream(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            LOG.warn("Error while reading Coupler engine output stream", e);
        }
    }
}
