package org.pacos.starter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.jdbc.DataSourceLoader;
import org.pacos.config.jdbc.ModuleJDBCService;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.rmi.RemoteRegistryService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class PacosTest {

    private Pacos pacos;
    private DataSource dataSource;
    private Path tempDir;

    @BeforeEach
    void init() throws IOException, AlreadyBoundException {
        this.tempDir = Files.createTempDirectory("pacos-test");
        System.setProperty("workingDir", tempDir.toString());
        String[] args = new String[]{"-DworkingDir=" + tempDir.toFile().getAbsolutePath()};

        try (MockedStatic<RemoteRegistryService> rmi = mockStatic(RemoteRegistryService.class)) {
            this.dataSource = DataSourceLoader.load();
            rmi.when(() -> RemoteRegistryService.registerRemoteInterface(any())).thenAnswer(inv -> null);
            this.pacos = new Pacos(List.of(args), dataSource);
            this.pacos.initializeDatabaseIfNotExists();
        }

    }

    @Test
    void whenModuleNotExistsThenIsNotValidInstallation() {
        List<AppArtifact> appArtifacts = new ArrayList<>();
        appArtifacts.add(new AppArtifact("org.pacos", "core", "1.0"));
        new ModuleJDBCService(dataSource).saveModuleConfiguration(appArtifacts);
        //when
        assertFalse(pacos.isValidInstallation());
    }

//    @Test
//    void whenModuleExistsThenIsValidInstallation() throws IOException {
//        List<AppArtifact> appArtifacts = new ArrayList<>();
//        appArtifacts.add(new AppArtifact("org.pacos", "core", "1.0"));
//        Path path = tempDir.resolve("lib\\org\\pacos\\core\\1.0");
//        path.toFile().mkdirs();
//        path.resolve("core-1.0.jar").toFile().createNewFile();
//        new ModuleJDBCService(dataSource).saveModuleConfiguration(appArtifacts);
//        //when
//        assertTrue(pacos.isValidInstallation());
//    }

}