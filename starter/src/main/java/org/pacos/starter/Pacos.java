package org.pacos.starter;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.pacos.config.jdbc.ModuleJDBCService;
import org.pacos.config.jdbc.PacosJdbcService;
import org.pacos.config.property.ApplicationProperties;
import org.pacos.config.property.PropertyName;
import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.PomDependencyResolver;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.ModuleConfiguration;
import org.pacos.config.rmi.RemoteRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pacos {
    private static final Logger LOG = LoggerFactory.getLogger(Pacos.class);
    private static final long RESTART_SIGNAL_GRACE_MILLIS = 250;
    private static final long INITIAL_RESTART_DELAY_MILLIS = 1_000;
    private static final long MAX_RESTART_DELAY_MILLIS = 30_000;

    private final List<String> args;
    private final ModuleJDBCService moduleInfoJdbcService;
    private final PacosJdbcService pacosJdbcService;
    private final Supplier<AppArtifact> engineArtifactSupplier;
    private final Function<AppArtifact, PacosProcess> engineProcessFactory;
    private final Object lifecycleLock = new Object();
    private final AtomicBoolean supervisionStarted = new AtomicBoolean();
    private final AtomicBoolean rmiRegistrationRetryStarted = new AtomicBoolean();
    private final RemoteAccess remoteAccess;
    private final BooleanSupplier rmiRegistration;
    private PacosProcess engine;
    private volatile boolean shutdownRequested;
    private boolean restartRequested;
    private volatile boolean remoteInterfaceRegistered;
    private boolean shutdownHookRegistered;

    public Pacos(List<String> args, DataSource dataSource) {
        this(args, dataSource, null, null, null);
    }

    Pacos(List<String> args,
          DataSource dataSource,
          Supplier<AppArtifact> engineArtifactSupplier,
          Function<AppArtifact, PacosProcess> engineProcessFactory) {
        this(args, dataSource, engineArtifactSupplier, engineProcessFactory, null);
    }

    Pacos(List<String> args,
          DataSource dataSource,
          Supplier<AppArtifact> engineArtifactSupplier,
          Function<AppArtifact, PacosProcess> engineProcessFactory,
          BooleanSupplier rmiRegistration) {
        this.args = args;
        this.moduleInfoJdbcService = new ModuleJDBCService(dataSource);
        this.pacosJdbcService = new PacosJdbcService(dataSource);
        this.engineArtifactSupplier = engineArtifactSupplier != null ? engineArtifactSupplier : this::getEngineArtifact;
        this.engineProcessFactory = engineProcessFactory != null
                ? engineProcessFactory
                : artifact -> new PacosProcess(artifact, this.args);
        this.remoteAccess = new RemoteAccess(this);
        this.rmiRegistration = rmiRegistration != null
                ? rmiRegistration
                : () -> RemoteRegistryService.tryRegisterRemoteInterface(remoteAccess);
    }

    void start() {
        superviseEngine();
    }

    boolean attachToRunningEngine() {
        return attachToRunningEngine(ProcessHandle.allProcesses());
    }

    boolean attachToRunningEngine(Stream<ProcessHandle> candidates) {
        installShutdownHookIfNeeded();

        AppArtifact installedEngine;
        try {
            installedEngine = moduleInfoJdbcService.loadEngineModule();
        } catch (RuntimeException e) {
            LOG.warn("Cannot load installed engine metadata; attach mode will not start a new server", e);
            return false;
        }

        if (installedEngine == null) {
            LOG.warn("No installed engine was found; attach mode will not start a new server");
            return false;
        }

        Path engineJar = WorkingDir.getLibPath().resolve(installedEngine.getJarPath());
        Optional<ProcessHandle> existingEngine;
        try {
            existingEngine = PacosProcess.findRunningEngine(WorkingDir.load(), engineJar, candidates);
        } catch (RuntimeException e) {
            LOG.warn("Cannot inspect running processes; attach mode will not start a new server", e);
            return false;
        }
        if (existingEngine.isEmpty()) {
            LOG.warn("No running PacOS engine matches working directory '{}' and artifact '{}'. "
                    + "Attach mode will not start a new server.", WorkingDir.load(), engineJar);
            return false;
        }

        synchronized (lifecycleLock) {
            if (engine != null || supervisionStarted.get()) {
                LOG.warn("Cannot attach because this starter already owns an engine lifecycle");
                return false;
            }
            engine = PacosProcess.attach(installedEngine, args, existingEngine.get());
        }
        registerRemoteInterfaceIfNeeded();
        LOG.info("Attached starter to existing PacOS engine process {}", existingEngine.get().pid());
        return true;
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

        // RMI is optional during initial startup, but restart recovery should keep trying in
        // case a transient port/configuration problem clears while the engine is still alive.
        if (rmiRegistrationRetryStarted.compareAndSet(false, true)) {
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

    boolean isAttachedToRunningEngine() {
        PacosProcess current;
        synchronized (lifecycleLock) {
            current = engine;
        }
        return current != null && current.isAttached();
    }

    /**
     * Runs the engine lifecycle under the starter's supervision. A requested restart and an
     * unexpected engine exit both lead to a replacement process, but only unexpected exits
     * use backoff. The same loop also supervises an engine to which recovery mode attached.
     */
    void awaitSupervision() {
        superviseEngine();
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

    private record SupervisionDecision(boolean stop, long nextDelayMillis) {
        private static SupervisionDecision stopSupervision() {
            return new SupervisionDecision(true, 0);
        }

        private static SupervisionDecision retryImmediately(long nextDelayMillis) {
            return new SupervisionDecision(false, nextDelayMillis);
        }
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

    void initializeDatabaseIfNotExists() {
        if (!moduleInfoJdbcService.tableExists()) {
            LOG.info("Installing database.....");
            pacosJdbcService.initDatabase();
        }
    }

    boolean isValidInstallation() {
        AppArtifact engineArtifact = moduleInfoJdbcService.loadEngineModule();
        List<AppArtifact> pacosResources = moduleInfoJdbcService.loadActiveModules();
        if (engineArtifact != null) {
            pacosResources.add(engineArtifact);
        } else {
            return false;
        }
        return pacosResources.stream().allMatch(a -> {
            if (WorkingDir.getLibPath().resolve(a.getJarPath()).toFile().exists()) {
                return true;
            } else {
                LOG.warn("The artifact {} not found. Trying to fix resources...", a.getJarPath());
                return false;
            }
        });

    }

    AppArtifact getEngineArtifact() {
        initializeDatabaseIfNotExists();

        String installedVersion = moduleInfoJdbcService.getInstalledSystemVersion();
        if (installedVersion != null && !installedVersion.equals("null") && isValidInstallation()) {
            LOG.info("Installed pacos version: '{}'", installedVersion);
            return moduleInfoJdbcService.loadEngineModule();
        } else {
            String versionToInstall = ApplicationProperties.get().getProperty(PropertyName.VERSION.getPropertyName());
            versionToInstall = versionToInstall != null ? versionToInstall : installedVersion;

            return downloadPacos(versionToInstall);
        }
    }

    private AppArtifact downloadPacos(String pacosVersion) {
        LOG.info("Starting installation process. This may take a few minutes. Please wait......");
        ModuleConfiguration currentModuleConfiguration = RepositoryClient.getModuleConfiguration(pacosVersion);

        PomDependencyResolver.resolve(currentModuleConfiguration.modules());

        moduleInfoJdbcService.saveModuleConfiguration(currentModuleConfiguration.modules());
        moduleInfoJdbcService.saveSystemVersion(currentModuleConfiguration.version());

        return currentModuleConfiguration.modules().stream().filter(m -> m.artifactName().equals("engine")
                        && m.groupId().equals("org.pacos"))
                .findFirst().orElseThrow(() -> new RuntimeException("PacOS engine not found"));
    }

    public void restartEngine() {
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

        // The RMI request originates in the engine process that must be stopped. Return from
        // the remote call first, then terminate that process so its caller can receive the ACK.
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
}
