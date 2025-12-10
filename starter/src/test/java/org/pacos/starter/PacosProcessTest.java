package org.pacos.starter;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.jdbc.ModuleLoader;
import org.pacos.config.repository.data.AppArtifact;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PacosProcessTest {

    @Test
    void whenRestartThenEngineRestarted() throws RemoteException {
        Pacos pacos = mock(Pacos.class);
        RemoteAccess remoteAccess = new RemoteAccess(pacos);

        remoteAccess.restart();

        verify(pacos, times(1)).restartEngine();
    }

    @Test
    void whenRestartThenLoggerInfoCalled() throws RemoteException {
        Pacos pacos = mock(Pacos.class);
        RemoteAccess remoteAccess = spy(new RemoteAccess(pacos));

        remoteAccess.restart();

        verify(pacos, times(1)).restartEngine();
    }

    @Test
    void whenCouplerProcessRunThenProcessStarts() {
        AppArtifact appArtifactMock = new AppArtifact("org.pacos", "engine", "1.0");

        List<String> args = List.of("--arg1", "--arg2");

        try (MockedStatic<ModuleLoader> loaderMock = mockStatic(ModuleLoader.class)) {
            loaderMock.when(ModuleLoader::load).thenReturn(List.of(Path.of(appArtifactMock.getJarPath())));

            PacosProcess pacosProcess = new PacosProcess(appArtifactMock, args);
            pacosProcess.run();

            assertNotNull(pacosProcess);
        }

    }

    @Test
    void whenCouplerProcessStopThenProcessStops() {
        AppArtifact appArtifactMock = new AppArtifact("org.pacos", "engine", "1.0");

        List<String> args = List.of("--arg1", "--arg2");
        try (MockedStatic<ModuleLoader> loaderMock = mockStatic(ModuleLoader.class)) {
            loaderMock.when(ModuleLoader::load).thenReturn(List.of(Path.of(appArtifactMock.getJarPath())));

            PacosProcess pacosProcess = new PacosProcess(appArtifactMock, args);

            pacosProcess.run();
            pacosProcess.stop();

            assertNotNull(pacosProcess);
        }
    }
}
