package org.pacos.starter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.pacos.config.jdbc.ModuleLoader;
import org.pacos.config.repository.data.AppArtifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PacosProcessTest {

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
    void whenFindRunningEngineMatchesWorkingDirectoryAndJarThenReturnProcess() {
        Path workingDirectory = Path.of("/tmp/pacos-attach-test").toAbsolutePath().normalize();
        Path engineJar = workingDirectory.resolve("lib/org/pacos/engine/3.5.0/engine-3.5.0.jar");
        ProcessHandle processHandle = mock(ProcessHandle.class);
        ProcessHandle.Info processInfo = mock(ProcessHandle.Info.class);
        when(processHandle.isAlive()).thenReturn(true);
        when(processHandle.info()).thenReturn(processInfo);
        when(processInfo.arguments()).thenReturn(Optional.of(new String[]{
                "-DworkingDir=" + workingDirectory,
                "-jar",
                engineJar.toString(),
                "org.springframework.boot.loader.launch.PropertiesLauncher"
        }));

        Optional<ProcessHandle> result = PacosProcess.findRunningEngine(
                workingDirectory, engineJar, Stream.of(processHandle));

        assertEquals(Optional.of(processHandle), result);
    }

    @Test
    void whenFindRunningEngineUsesAnotherJarThenReturnEmpty() {
        Path workingDirectory = Path.of("/tmp/pacos-attach-test").toAbsolutePath().normalize();
        Path engineJar = workingDirectory.resolve("lib/org/pacos/engine/3.5.0/engine-3.5.0.jar");
        ProcessHandle processHandle = mock(ProcessHandle.class);
        ProcessHandle.Info processInfo = mock(ProcessHandle.Info.class);
        when(processHandle.isAlive()).thenReturn(true);
        when(processHandle.info()).thenReturn(processInfo);
        when(processInfo.arguments()).thenReturn(Optional.of(new String[]{
                "-DworkingDir=" + workingDirectory,
                "-jar",
                workingDirectory.resolve("lib/org/pacos/engine/3.4.0/engine-3.4.0.jar").toString(),
                "org.springframework.boot.loader.launch.PropertiesLauncher"
        }));

        Optional<ProcessHandle> result = PacosProcess.findRunningEngine(
                workingDirectory, engineJar, Stream.of(processHandle));

        assertTrue(result.isEmpty());
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

    @Test
    void whenGracefulTerminationTimesOutThenForcedTerminationIsWaitedFor() throws InterruptedException {
        Process process = mock(Process.class);
        when(process.waitFor(1, TimeUnit.SECONDS)).thenReturn(false);
        when(process.waitFor()).thenReturn(0);

        PacosProcess.terminateProcessWithTimeout(process, 1);

        InOrder ordered = inOrder(process);
        ordered.verify(process).destroy();
        ordered.verify(process).waitFor(1, TimeUnit.SECONDS);
        ordered.verify(process).destroyForcibly();
        ordered.verify(process).waitFor();
    }

    @Test
    void whenProcessWritesToBothStreamsThenBothOutputsArePrinted() throws InterruptedException {
        Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream("stdout-message\n".getBytes(StandardCharsets.UTF_8)));
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream("stderr-message\n".getBytes(StandardCharsets.UTF_8)));
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8);

        try {
            System.setOut(capturedOut);
            PacosProcess.printProcessStreams(process);
        } finally {
            System.setOut(originalOut);
            capturedOut.close();
        }

        String printedOutput = output.toString(StandardCharsets.UTF_8);
        assertTrue(printedOutput.contains("stdout-message"));
        assertTrue(printedOutput.contains("stderr-message"));
    }

}