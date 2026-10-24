package org.pacos.starter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.AlreadyBoundException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.pacos.config.jdbc.DataSourceLoader;
import org.pacos.config.jdbc.ModuleJDBCService;
import org.pacos.config.property.ApplicationProperties;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.rmi.RemoteRegistryService;

class PacosTest {

    private Pacos pacos;
    private DataSource dataSource;
    @TempDir
    private Path tempDir;
    private String originalEnvironment;
    private String originalWorkingDir;
    private String originalDatasourceUrl;

    @BeforeEach
    void init() throws AlreadyBoundException {
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
            rmi.when(() -> RemoteRegistryService.registerRemoteInterface(any())).thenAnswer(inv -> null);
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

    private void createJar(AppArtifact artifact) throws IOException {
        Path jar = tempDir.resolve("lib").resolve(artifact.getJarPath());
        Files.createDirectories(jar.getParent());
        Files.createFile(jar);
    }

}