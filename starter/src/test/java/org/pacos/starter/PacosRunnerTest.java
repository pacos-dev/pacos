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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class PacosRunnerTest {

    @TempDir
    private File tempDir;

    @Test
    void whenMainThenRunEngineWithoutExceptions() {
        String[] args = new String[]{"-DworkingDir=" + tempDir.getAbsolutePath(), "-D" + PropertyName.RMI_PORT.getPropertyName() + "=1999"};

        try (MockedStatic<RepositoryClient> repositoryMock = mockStatic(RepositoryClient.class);
             MockedStatic<PomDependencyResolver> dependencyResolver = mockStatic(PomDependencyResolver.class);
             MockedStatic<PacosProcess> pacosProcess = mockStatic(PacosProcess.class);
             MockedStatic<LogbackConfig> logback = mockStatic(LogbackConfig.class);
        ) {
            repositoryMock.when(() -> RepositoryClient.getModuleConfiguration(any())).thenReturn(
                    new ModuleConfiguration("1.0",
                            List.of(new AppArtifact("org.pacos", "engine", "1.0"))));
            dependencyResolver.when(() -> PomDependencyResolver.resolve(any())).thenAnswer(inv -> null);
            pacosProcess.when(() -> PacosProcess.startProcess(any())).thenReturn(Mockito.mock(Process.class));
            logback.when(() -> LogbackConfig.configure(any())).thenAnswer(inv -> null);

            assertDoesNotThrow(() -> PacosRunner.main(args));
        }
    }

}