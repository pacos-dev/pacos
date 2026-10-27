package org.pacos.starter;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

import org.pacos.config.repository.data.AppArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class EngineSupervisor {
    private static final Logger LOG = LoggerFactory.getLogger(EngineSupervisor.class);
    private static final long RESTART_SIGNAL_GRACE_MILLIS = 250;
    private static final long INITIAL_RESTART_DELAY_MILLIS = 1_000;
    private static final long MAX_RESTART_DELAY_MILLIS = 30_000;

    private final List<String> args;
    private final Supplier<AppArtifact> engineArtifactSupplier;
    private final Function<AppArtifact, PacosProcess> engineProcessFactory;
    private final BooleanSupplier rmiRegistration;
    private final Object lifecycleLock = new Object();
    private final AtomicBoolean supervisionStarted = new AtomicBoolean();
    private final AtomicBoolean rmiRegistrationRetryStarted = new AtomicBoolean();
    private PacosProcess engine;
    private volatile boolean shutdownRequested;
    private boolean restartRequested;
    private volatile boolean remoteInterfaceRegistered;
    private boolean shutdownHookRegistered;

    EngineSupervisor(
            List<String> args,
            Supplier<AppArtifact> engineArtifactSupplier,
            Function<AppArtifact, PacosProcess> engineProcessFactory,
            BooleanSupplier rmiRegistration) {
        this.args = args;
        this.engineArtifactSupplier = engineArtifactSupplier;
        this.engineProcessFactory = engineProcessFactory;
        this.rmiRegistration = rmiRegistration;
    }

    void start() {
        superviseEngine();
    }

    void prepareForAttach() {
        installShutdownHookIfNeeded();
    }

    boolean attachToEngine(AppArtifact installedEngine, ProcessHandle process) {
        synchronized (lifecycleLock) {
            if (engine != null || supervisionStarted.get()) {
                LOG.warn("Cannot attach because this starter already owns an engine lifecycle");
                return false;
            }
            engine = PacosProcess.attach(installedEngine, args, process);
        }
        registerRemoteInterfaceIfNeeded();
        LOG.info("Attached starter to existing PacOS engine process {}", process.pid());
        return true;
    }

    boolean isAttachedToRunningEngine() {
        PacosProcess current;
        synchronized (lifecycleLock) {
            current = engine;
        }
        return current != null && current.isAttached();
    }

    void awaitSupervision() {
        superviseEngine();
    }

    void restartEngine() {
        LOG.info("Restart requested by the PacOS engine");
        PacosProcess current;
        synchronized (lifecycleLock) {
            if (shutdownRequested) {
                LOG.warn("Ignoring engine restart request because the starter is shutting down");
                return;
            }
            if (restartRequested) {
                LOG.info("An engine restart is already pending");
                return;
            }
            current = engine;
            if (current == null) {
                LOG.warn("Restart requested while no engine process is assigned; the supervisor will start one");
                return;
            }
            restartRequested = true;
        }

        // Let the RMI caller receive its acknowledgement before stopping its own process.
        Thread stopWorker = new Thread(() -> {
            try {
                Thread.sleep(RESTART_SIGNAL_GRACE_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.warn("Engine restart worker was interrupted before stopping the old process", e);
                return;
            }

            if (!current.stopAndAwait()) {
                synchronized (lifecycleLock) {
                    if (engine == current) {
                        restartRequested = false;
                    }
                }
                LOG.error("Restart aborted because the previous engine process is still running");
                return;
            }
            LOG.info("Engine stop confirmed; the supervisor will start its replacement");
        }, "pacos-engine-restart-request");
        stopWorker.setDaemon(true);
        stopWorker.start();
    }

    private void registerRemoteInterfaceIfNeeded() {
        synchronized (this) {
            if (remoteInterfaceRegistered) {
                return;
            }
            remoteInterfaceRegistered = rmiRegistration.getAsBoolean();
            if (remoteInterfaceRegistered) {
                LOG.info("PacOS restart endpoint is available over RMI");
                return;
            }
        }

        if (rmiRegistrationRetryStarted.compareAndSet(false, true)) {
            // RMI is optional during startup; keep retrying while the engine is alive.
            Thread retryThread = new Thread(this::retryRemoteInterfaceRegistration,
                    "pacos-rmi-registration-retry");
            retryThread.setDaemon(true);
            retryThread.start();
        }
    }

    private void retryRemoteInterfaceRegistration() {
        long retryDelayMillis = 1_000;
        while (!shutdownRequested && !remoteInterfaceRegistered) {
            if (!waitBeforeRetry(retryDelayMillis)) {
                return;
            }
            registerRemoteInterfaceIfNeeded();
            retryDelayMillis = Math.min(retryDelayMillis * 2, MAX_RESTART_DELAY_MILLIS);
        }
    }

    private void superviseEngine() {
        installShutdownHookIfNeeded();
        if (!supervisionStarted.compareAndSet(false, true)) {
            LOG.warn("PacOS engine supervision is already running in this starter");
            return;
        }

        long retryDelayMillis = INITIAL_RESTART_DELAY_MILLIS;
        while (isSupervisionActive()) {
            SupervisionDecision decision = superviseEngineOnce(retryDelayMillis);
            if (decision.stop()) {
                return;
            }
            retryDelayMillis = decision.nextDelayMillis();
        }
    }

    private boolean isSupervisionActive() {
        return !shutdownRequested && !Thread.currentThread().isInterrupted();
    }

    private SupervisionDecision superviseEngineOnce(long retryDelayMillis) {
        PacosProcess current;
        try {
            current = getOrCreateEngine();
        } catch (RuntimeException e) {
            LOG.error("Could not prepare the PacOS engine; retrying in {} ms", retryDelayMillis, e);
            return retryAfter(retryDelayMillis, nextRetryDelay(retryDelayMillis));
        }
        if (current == null) {
            return SupervisionDecision.stopSupervision();
        }

        registerRemoteInterfaceIfNeeded();
        int exitCode = runEngine(current);
        boolean requestedRestart = clearCurrentEngine(current);

        if (!isSupervisionActive()) {
            return SupervisionDecision.stopSupervision();
        }
        if (requestedRestart) {
            LOG.info("Engine stopped for a requested restart; starting a replacement");
            return SupervisionDecision.retryImmediately(INITIAL_RESTART_DELAY_MILLIS);
        }

        LOG.warn("PacOS engine exited unexpectedly with code {}. Restarting in {} ms",
                exitCode, retryDelayMillis);
        return retryAfter(retryDelayMillis, nextRetryDelay(retryDelayMillis));
    }

    private PacosProcess getOrCreateEngine() {
        synchronized (lifecycleLock) {
            if (engine != null) {
                return engine;
            }
            if (shutdownRequested) {
                return null;
            }
        }

        AppArtifact engineArtifact = engineArtifactSupplier.get();
        PacosProcess candidate = engineProcessFactory.apply(engineArtifact);
        synchronized (lifecycleLock) {
            if (shutdownRequested) {
                candidate.stopAndAwait();
                return null;
            }
            if (engine == null) {
                engine = candidate;
            }
            return engine;
        }
    }

    private int runEngine(PacosProcess current) {
        try {
            return current.run();
        } catch (RuntimeException e) {
            LOG.error("Engine process wrapper failed; the supervisor will recover it", e);
            return -1;
        }
    }

    private boolean clearCurrentEngine(PacosProcess current) {
        synchronized (lifecycleLock) {
            if (engine == current) {
                engine = null;
            }
            boolean requestedRestart = restartRequested;
            restartRequested = false;
            return requestedRestart;
        }
    }

    private SupervisionDecision retryAfter(long delayMillis, long nextDelayMillis) {
        if (!waitBeforeRetry(delayMillis)) {
            return SupervisionDecision.stopSupervision();
        }
        return SupervisionDecision.retryImmediately(nextDelayMillis);
    }

    private static long nextRetryDelay(long currentDelayMillis) {
        return Math.min(currentDelayMillis * 2, MAX_RESTART_DELAY_MILLIS);
    }

    private boolean waitBeforeRetry(long delayMillis) {
        if (shutdownRequested || Thread.currentThread().isInterrupted()) {
            return false;
        }
        try {
            Thread.sleep(delayMillis);
            return !shutdownRequested;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.info("PacOS engine supervision interrupted");
            return false;
        }
    }

    private void installShutdownHookIfNeeded() {
        synchronized (lifecycleLock) {
            if (shutdownHookRegistered) {
                return;
            }
            shutdownHookRegistered = true;
        }

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                PacosProcess current;
                synchronized (lifecycleLock) {
                    shutdownRequested = true;
                    current = engine;
                }
                if (current != null) {
                    current.stopAndAwait();
                }
            }, "pacos-engine-supervisor-shutdown"));
        } catch (IllegalStateException e) {
            synchronized (lifecycleLock) {
                shutdownRequested = true;
            }
            LOG.debug("JVM shutdown is already in progress; cannot register the PacOS supervisor hook", e);
        }
    }

    private record SupervisionDecision(boolean stop, long nextDelayMillis) {
        private static SupervisionDecision stopSupervision() {
            return new SupervisionDecision(true, 0);
        }

        private static SupervisionDecision retryImmediately(long nextDelayMillis) {
            return new SupervisionDecision(false, nextDelayMillis);
        }
    }
}
