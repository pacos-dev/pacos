package org.pacos.starter;

import javax.sql.DataSource;
import java.util.List;

import org.pacos.config.jdbc.ModuleJDBCService;
import org.pacos.config.jdbc.PacosJdbcService;
import org.pacos.config.property.ApplicationProperties;
import org.pacos.config.property.PropertyName;
import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.PomDependencyResolver;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.ModuleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class EngineInstaller {
    private static final Logger LOG = LoggerFactory.getLogger(EngineInstaller.class);

    private final ModuleJDBCService moduleInfoJdbcService;
    private final PacosJdbcService pacosJdbcService;

    EngineInstaller(DataSource dataSource) {
        this.moduleInfoJdbcService = new ModuleJDBCService(dataSource);
        this.pacosJdbcService = new PacosJdbcService(dataSource);
    }

    AppArtifact loadInstalledEngine() {
        return moduleInfoJdbcService.loadEngineModule();
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

        return currentModuleConfiguration.modules().stream()
                .filter(module -> module.artifactName().equals("engine")
                        && module.groupId().equals("org.pacos"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("PacOS engine not found"));
    }
}
