package org.pacos.starter;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.rmi.RemoteRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pacos {
    private static final Logger LOG = LoggerFactory.getLogger(Pacos.class);

    private final EngineInstaller engineInstaller;
    private final EngineSupervisor engineSupervisor;

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
        this.engineInstaller = new EngineInstaller(dataSource);
        Supplier<AppArtifact> resolvedArtifactSupplier = engineArtifactSupplier != null
                ? engineArtifactSupplier
                : engineInstaller::getEngineArtifact;
        Function<AppArtifact, PacosProcess> resolvedProcessFactory = engineProcessFactory != null
                ? engineProcessFactory
                : artifact -> new PacosProcess(artifact, args);
        RemoteAccess remoteAccess = new RemoteAccess(this);
        BooleanSupplier resolvedRmiRegistration = rmiRegistration != null
                ? rmiRegistration
                : () -> RemoteRegistryService.tryRegisterRemoteInterface(remoteAccess);
        this.engineSupervisor = new EngineSupervisor(
                args, resolvedArtifactSupplier, resolvedProcessFactory, resolvedRmiRegistration);
    }

    void start() {
        engineSupervisor.start();
    }

    boolean attachToRunningEngine() {
        return attachToRunningEngine(ProcessHandle.allProcesses());
    }

    boolean attachToRunningEngine(Stream<ProcessHandle> candidates) {
        engineSupervisor.prepareForAttach();

        AppArtifact installedEngine;
        try {
            installedEngine = engineInstaller.loadInstalledEngine();
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

        return engineSupervisor.attachToEngine(installedEngine, existingEngine.get());
    }

    boolean isAttachedToRunningEngine() {
        return engineSupervisor.isAttachedToRunningEngine();
    }

    /**
     * Runs the engine lifecycle under the starter's supervision. A requested restart and an
     * unexpected engine exit both lead to a replacement process, but only unexpected exits
     * use backoff. The same loop also supervises an engine to which recovery mode attached.
     */
    void awaitSupervision() {
        engineSupervisor.awaitSupervision();
    }

    void initializeDatabaseIfNotExists() {
        engineInstaller.initializeDatabaseIfNotExists();
    }

    boolean isValidInstallation() {
        return engineInstaller.isValidInstallation();
    }

    AppArtifact getEngineArtifact() {
        return engineInstaller.getEngineArtifact();
    }

    public void restartEngine() {
        engineSupervisor.restartEngine();
    }
}
