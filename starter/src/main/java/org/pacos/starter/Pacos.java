package org.pacos.starter;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.Optional;
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

    private final List<String> args;
    private final ModuleJDBCService moduleInfoJdbcService;
    private final PacosJdbcService pacosJdbcService;
    private PacosProcess engine;
    private boolean remoteInterfaceRegistered;

    public Pacos(List<String> args, DataSource dataSource) {
        this.args = args;
        this.moduleInfoJdbcService = new ModuleJDBCService(dataSource);
        this.pacosJdbcService = new PacosJdbcService(dataSource);
    }

    void start() {
        registerRemoteInterfaceIfNeeded();
        AppArtifact engineArtifact = getEngineArtifact();

        this.engine = new PacosProcess(engineArtifact, args);
        this.engine.run();
    }

    boolean attachToRunningEngine() {
        return attachToRunningEngine(ProcessHandle.allProcesses());
    }

    boolean attachToRunningEngine(Stream<ProcessHandle> candidates) {
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
        Optional<ProcessHandle> existingEngine = PacosProcess.findRunningEngine(
                WorkingDir.load(), engineJar, candidates);
        if (existingEngine.isEmpty()) {
            LOG.warn("No running PacOS engine matches working directory '{}' and artifact '{}'. "
                    + "Attach mode will not start a new server.", WorkingDir.load(), engineJar);
            return false;
        }

        this.engine = PacosProcess.attach(installedEngine, args, existingEngine.get());
        registerRemoteInterfaceIfNeeded();
        LOG.info("Attached starter to existing PacOS engine process {}", existingEngine.get().pid());
        return true;
    }

    private void registerRemoteInterfaceIfNeeded() {
        if (!remoteInterfaceRegistered) {
            RemoteRegistryService.registerRemoteInterface(new RemoteAccess(this));
            remoteInterfaceRegistered = true;
        }
    }

    boolean isAttachedToRunningEngine() {
        return engine != null && engine.isAttached();
    }

    void awaitSupervision() {
        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Starter supervision was interrupted", e);
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
        LOG.info("Restarting engine");
        if (engine != null && !engine.stopAndAwait()) {
            LOG.error("Restart aborted because the previous engine process is still running");
            return;
        }
        LOG.info("Starting engine");
        start();
    }
}