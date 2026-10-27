package org.pacos.starter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.MockedStatic;
import org.pacos.config.jdbc.DataSourceLoader;
import org.pacos.config.jdbc.ModuleJDBCService;
import org.pacos.config.jdbc.ModuleLoader;
import org.pacos.config.property.ApplicationProperties;
import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.rmi.RemoteRegistryService;

@Isolated("Shares JVM-wide system properties or other global test resources; keep isolated during parallel runs.")
class PacosTest {

    private Pacos pacos;
    private DataSource dataSource;
    @TempDir
    private Path tempDir;
    private String originalEnvironment;
    private String originalWorkingDir;
    private String originalDatasourceUrl;

    @BeforeEach
    void init() {
        originalEnvironment = System.getProperty("pacos.environment");
        originalWorkingDir = System.getProperty("workingDir");
        originalDatasourceUrl = System.getProperty("spring.datasource.url");
        System.setProperty("pacos.environment", "test");
        System.setProperty("workingDir", tempDir.toString());
        System.setProperty("spring.datasource.url", "jdbc:hsqldb:mem:pacos_test_" +
                UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1");
        ApplicationProperties.reloadProperties();
        String[] args = new String[]{"-DworkingDir=" + tempDir.toFile().getAbsolutePath()};

        try (MockedStatic<RemoteRegistryService> rmi = mockStatic(RemoteRegistryService.class)) {
            this.dataSource = DataSourceLoader.load();
            rmi.when(() -> RemoteRegistryService.tryRegisterRemoteInterface(any())).thenReturn(true);
            this.pacos = new Pacos(List.of(args), dataSource);
            this.pacos.initializeDatabaseIfNotExists();
        }

    }

    @AfterEach
    void restoreSystemProperties() throws Exception {
        try {
            if (dataSource != null) {
                try (var connection = dataSource.getConnection();
                     var statement = connection.createStatement()) {
                    statement.execute("SHUTDOWN");
                }
            }
        } finally {
            restoreSystemProperty("pacos.environment", originalEnvironment);
            restoreSystemProperty("workingDir", originalWorkingDir);
            restoreSystemProperty("spring.datasource.url", originalDatasourceUrl);
            ApplicationProperties.reloadProperties();
        }
    }

    private static void restoreSystemProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    @Test
    void whenIsValidInstallationCalledWithoutEngineThenReturnsFalse() {
        AppArtifact core = new AppArtifact("org.pacos", "core", "1.0");
        new ModuleJDBCService(dataSource).saveModuleConfiguration(List.of(core));

        assertFalse(pacos.isValidInstallation());
    }

    @Test
    void whenIsValidInstallationCalledWithMissingModuleJarThenReturnsFalse() throws IOException {
        AppArtifact engine = new AppArtifact("org.pacos", "engine", "1.0");
        AppArtifact core = new AppArtifact("org.pacos", "core", "1.0");
        createJar(engine);
        new ModuleJDBCService(dataSource).saveModuleConfiguration(List.of(engine, core));

        assertFalse(pacos.isValidInstallation());
    }

    @Test
    void whenIsValidInstallationCalledWithAllModuleJarsThenReturnsTrue() throws IOException {
        AppArtifact engine = new AppArtifact("org.pacos", "engine", "1.0");
        AppArtifact core = new AppArtifact("org.pacos", "core", "1.0");
        createJar(engine);
        createJar(core);
        new ModuleJDBCService(dataSource).saveModuleConfiguration(List.of(engine, core));

        assertTrue(pacos.isValidInstallation());
    }

    @Test
    void whenAttachFindsExistingEngineThenReuseProcessWithoutStartingAnotherEngine() throws IOException {
        AppArtifact engine = new AppArtifact("org.pacos", "engine", "1.0");
        createJar(engine);
        new ModuleJDBCService(dataSource).saveModuleConfiguration(List.of(engine));

        ProcessHandle existingEngine = mock(ProcessHandle.class);
        ProcessHandle.Info processInfo = mock(ProcessHandle.Info.class);
        Path engineJar = WorkingDir.getLibPath().resolve(engine.getJarPath()).toAbsolutePath().normalize();
        when(existingEngine.isAlive()).thenReturn(true, false);
        when(existingEngine.pid()).thenReturn(4567L);
        when(existingEngine.info()).thenReturn(processInfo);
        when(processInfo.arguments()).thenReturn(Optional.of(new String[]{
                "-DworkingDir=" + WorkingDir.load().toAbsolutePath().normalize(),
                "-jar",
                engineJar.toString(),
                "org.springframework.boot.loader.launch.PropertiesLauncher"
        }));

        try (MockedStatic<ModuleLoader> moduleLoader = mockStatic(ModuleLoader.class);
             MockedStatic<RemoteRegistryService> rmi = mockStatic(RemoteRegistryService.class)) {
            moduleLoader.when(ModuleLoader::load).thenReturn(List.of(engineJar));
            rmi.when(() -> RemoteRegistryService.tryRegisterRemoteInterface(any())).thenReturn(true);

            assertTrue(pacos.attachToRunningEngine(Stream.of(existingEngine)));
            assertTrue(pacos.isAttachedToRunningEngine());
        }
    }

    @Test
    void whenAttachCannotFindMatchingEngineThenDoNotStartServerOrRegisterRestartEndpoint() throws IOException {
        AppArtifact engine = new AppArtifact("org.pacos", "engine", "1.0");
        createJar(engine);
        new ModuleJDBCService(dataSource).saveModuleConfiguration(List.of(engine));

        ProcessHandle unrelatedProcess = mock(ProcessHandle.class);
        ProcessHandle.Info processInfo = mock(ProcessHandle.Info.class);
        when(unrelatedProcess.isAlive()).thenReturn(true);
        when(unrelatedProcess.info()).thenReturn(processInfo);
        when(processInfo.arguments()).thenReturn(Optional.of(new String[]{
                "-DworkingDir=" + WorkingDir.load().toAbsolutePath().normalize(),
                "-jar",
                tempDir.resolve("other-engine.jar").toString(),
                "org.springframework.boot.loader.launch.PropertiesLauncher"
        }));

        try (MockedStatic<RemoteRegistryService> rmi = mockStatic(RemoteRegistryService.class)) {
            assertFalse(pacos.attachToRunningEngine(Stream.of(unrelatedProcess)));
            assertFalse(pacos.isAttachedToRunningEngine());
            rmi.verify(() -> RemoteRegistryService.tryRegisterRemoteInterface(any()), never());
        }
    }

    private void createJar(AppArtifact artifact) throws IOException {
        Path jar = tempDir.resolve("lib").resolve(artifact.getJarPath());
        Files.createDirectories(jar.getParent());
        Files.createFile(jar);
    }


    @Test
    void whenAttachIsRequestedWithoutInstalledEngineThenReturnFalse() {
        assertFalse(pacos.attachToRunningEngine(Stream.empty()));
        assertFalse(pacos.isAttachedToRunningEngine());
    }

    @Test
    void whenRestartIsRequestedWithoutAssignedEngineThenReturnWithoutStartingProcess() {
        assertDoesNotThrow(() -> pacos.restartEngine());
    }

}
