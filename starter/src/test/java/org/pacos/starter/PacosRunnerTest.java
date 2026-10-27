package org.pacos.starter;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.config.property.PropertyName;
import org.pacos.config.repository.PomDependencyResolver;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.ModuleConfiguration;
import org.pacos.config.rmi.RemoteRegistryService;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

class PacosRunnerTest {

    @TempDir
    private File tempDir;

    @Test
    void whenControlFlagsArePassedThenRemoveThemFromEngineArguments() {
        String[] args = new String[]{"--attach", "--recover", "-DworkingDir=/tmp/pacos", "-Drmi.port=1999"};

        assertArrayEquals(new String[]{"-DworkingDir=/tmp/pacos", "-Drmi.port=1999"},
                PacosRunner.engineArguments(args));
    }

    @Test
    void whenAttachModeFindsEngineThenSuperviseItWithoutStartingAnotherEngine() {
        Pacos pacos = mock(Pacos.class);
        when(pacos.attachToRunningEngine()).thenReturn(true);

        PacosRunner.run(pacos, true, false);

        verify(pacos).attachToRunningEngine();
        verify(pacos).awaitSupervision();
        verify(pacos, never()).start();
    }

    @Test
    void whenAttachModeCannotFindEngineThenDoNotStartAnotherOne() {
        Pacos pacos = mock(Pacos.class);
        when(pacos.attachToRunningEngine()).thenReturn(false);

        PacosRunner.run(pacos, true, false);

        verify(pacos).attachToRunningEngine();
        verify(pacos, never()).awaitSupervision();
        verify(pacos, never()).start();
    }

    @Test
    void whenRecoveryModeCannotFindEngineThenStartANewEngine() {
        Pacos pacos = mock(Pacos.class);
        when(pacos.attachToRunningEngine()).thenReturn(false);

        PacosRunner.run(pacos, false, true);

        verify(pacos).attachToRunningEngine();
        verify(pacos, never()).awaitSupervision();
        verify(pacos).start();
    }

    @Test
    void whenMainThenRunEngineWithoutExceptions() {
        String[] args = new String[]{"-DworkingDir=" + tempDir.getAbsolutePath(), "-D" + PropertyName.RMI_PORT.getPropertyName() + "=1999", "-Dpacos.environment=test"};

        Process engineProcess = Mockito.mock(Process.class);
        Mockito.when(engineProcess.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(new byte[0]));
        Mockito.when(engineProcess.getErrorStream()).thenReturn(new java.io.ByteArrayInputStream(new byte[0]));
        Mockito.when(engineProcess.waitFor()).thenAnswer(invocation -> {
            Thread.currentThread().interrupt();
            return 0;
        });

        try (MockedStatic<RepositoryClient> repositoryMock = mockStatic(RepositoryClient.class);
             MockedStatic<PomDependencyResolver> dependencyResolver = mockStatic(PomDependencyResolver.class);
             MockedStatic<PacosProcess> pacosProcess = mockStatic(PacosProcess.class);
             MockedStatic<LogbackConfig> logback = mockStatic(LogbackConfig.class);
             MockedStatic<RemoteRegistryService> rmi = mockStatic(RemoteRegistryService.class);
        ) {
            repositoryMock.when(() -> RepositoryClient.getModuleConfiguration(any())).thenReturn(
                    new ModuleConfiguration("1.0",
                            List.of(new AppArtifact("org.pacos", "engine", "1.0"))));
            dependencyResolver.when(() -> PomDependencyResolver.resolve(any())).thenAnswer(inv -> null);
            pacosProcess.when(() -> PacosProcess.startProcess(any())).thenReturn(engineProcess);
            rmi.when(() -> RemoteRegistryService.registerRemoteInterface(any())).thenReturn(true);
            logback.when(() -> LogbackConfig.configure(any())).thenAnswer(inv -> null);

            try {
                assertDoesNotThrow(() -> PacosRunner.main(args));
            } finally {
                Thread.interrupted();
            }
        }
    }

}
