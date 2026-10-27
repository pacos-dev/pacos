package org.pacos.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.pacos.config.jdbc.ModuleLoader;
import org.pacos.config.repository.data.AppArtifact;

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
        Path workingDirectory = Path.of("pacos-attach-test").toAbsolutePath().normalize();
        Path engineJar = workingDirectory.resolve("lib/org/pacos/engine/3.5.0/engine-3.5.0.jar");
        ProcessHandle processHandle = mock(ProcessHandle.class);
        ProcessHandle.Info processInfo = mock(ProcessHandle.Info.class);
        when(processHandle.isAlive()).thenReturn(true);
        when(processHandle.info()).thenReturn(processInfo);
        when(processInfo.arguments()).thenReturn(Optional.of(new String[] {
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
        Path workingDirectory = Path.of("pacos-attach-test").toAbsolutePath().normalize();
        Path engineJar = workingDirectory.resolve("lib/org/pacos/engine/3.5.0/engine-3.5.0.jar");
        ProcessHandle processHandle = mock(ProcessHandle.class);
        ProcessHandle.Info processInfo = mock(ProcessHandle.Info.class);
        when(processHandle.isAlive()).thenReturn(true);
        when(processHandle.info()).thenReturn(processInfo);
        when(processInfo.arguments()).thenReturn(Optional.of(new String[] {
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
    void whenFindRunningEngineHasNoProcessArgumentsThenReturnEmpty() {
        Path workingDirectory = Path.of("pacos-attach-test").toAbsolutePath().normalize();
        ProcessHandle processHandle = mock(ProcessHandle.class);
        ProcessHandle.Info processInfo = mock(ProcessHandle.Info.class);
        when(processHandle.isAlive()).thenReturn(true);
        when(processHandle.info()).thenReturn(processInfo);
        when(processInfo.arguments()).thenReturn(Optional.empty());

        Optional<ProcessHandle> result = PacosProcess.findRunningEngine(
                workingDirectory, workingDirectory.resolve("engine.jar"), Stream.of(processHandle));

        assertTrue(result.isEmpty());
    }

    @Test
    void whenFindRunningEngineSkipsDeadAndUnrelatedProcessesThenReturnMatchingProcess() {
        Path workingDirectory = Path.of("pacos-attach-test").toAbsolutePath().normalize();
        Path engineJar = workingDirectory.resolve("lib/engine.jar");
        ProcessHandle deadProcess = mock(ProcessHandle.class);
        when(deadProcess.isAlive()).thenReturn(false);
        ProcessHandle unrelatedProcess = processWithArguments(
                "-DworkingDir=" + workingDirectory.resolve("other"), "-jar", engineJar.toString());
        when(unrelatedProcess.isAlive()).thenReturn(true);
        ProcessHandle matchingProcess = processWithArguments(
                "-DworkingDir=.", "-jar", "lib/engine.jar");
        when(matchingProcess.isAlive()).thenReturn(true);

        Optional<ProcessHandle> result = PacosProcess.findRunningEngine(
                workingDirectory, engineJar, Stream.of(deadProcess, unrelatedProcess, matchingProcess));

        assertEquals(Optional.of(matchingProcess), result);
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
    void whenProcessIsStoppedBeforeRunThenReturnZeroWithoutStartingIt() {
        AppArtifact artifact = new AppArtifact("org.pacos", "engine", "1.0");

        try (MockedStatic<ModuleLoader> loaderMock = mockStatic(ModuleLoader.class)) {
            loaderMock.when(ModuleLoader::load).thenReturn(List.of());

            PacosProcess process = new PacosProcess(artifact, List.of());
            process.stop();

            assertEquals(0, process.run());
        }
    }

    @Test
    void whenProcessCannotStartThenReturnMinusOne() {
        AppArtifact artifact = new AppArtifact("org.pacos", "engine", "1.0");

        try (MockedStatic<ModuleLoader> loaderMock = mockStatic(ModuleLoader.class);
             MockedStatic<PacosProcess> processMock = mockStatic(PacosProcess.class, CALLS_REAL_METHODS)) {
            loaderMock.when(ModuleLoader::load).thenReturn(List.of());
            processMock.when(() -> PacosProcess.startProcess(org.mockito.ArgumentMatchers.any()))
                    .thenThrow(new IOException("start failed"));

            PacosProcess process = new PacosProcess(artifact, List.of());

            assertEquals(-1, process.run());
        }
    }

    @Test
    void whenChildProcessExitsThenRunReturnsItsExitCode() throws Exception {
        AppArtifact artifact = new AppArtifact("org.pacos", "engine", "1.0");
        Process child = mock(Process.class);
        when(child.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(child.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(child.waitFor()).thenReturn(23);

        try (MockedStatic<ModuleLoader> loader = mockStatic(ModuleLoader.class);
             MockedStatic<PacosProcess> processMock = mockStatic(PacosProcess.class, CALLS_REAL_METHODS)) {
            loader.when(ModuleLoader::load).thenReturn(List.of());
            processMock.when(() -> PacosProcess.startProcess(org.mockito.ArgumentMatchers.any()))
                    .thenReturn(child);

            assertEquals(23, new PacosProcess(artifact, List.of()).run());
        }
    }

    @Test
    void whenWaitingForChildProcessIsInterruptedThenForceStopAndRestoreInterrupt() throws Exception {
        AppArtifact artifact = new AppArtifact("org.pacos", "engine", "1.0");
        Process child = mock(Process.class);
        when(child.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(child.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(child.waitFor()).thenThrow(new InterruptedException());

        try (MockedStatic<ModuleLoader> loader = mockStatic(ModuleLoader.class);
             MockedStatic<PacosProcess> processMock = mockStatic(PacosProcess.class, CALLS_REAL_METHODS)) {
            loader.when(ModuleLoader::load).thenReturn(List.of());
            processMock.when(() -> PacosProcess.startProcess(org.mockito.ArgumentMatchers.any()))
                    .thenReturn(child);

            try {
                assertEquals(-1, new PacosProcess(artifact, List.of()).run());
                assertTrue(Thread.currentThread().isInterrupted());
                verify(child).destroyForcibly();
            } finally {
                Thread.interrupted();
            }
        }
    }

    @Test
    void whenAttachedProcessHasExitedThenRunReturnsZero() {
        AppArtifact artifact = new AppArtifact("org.pacos", "engine", "1.0");
        ProcessHandle handle = mock(ProcessHandle.class);
        when(handle.onExit()).thenReturn(CompletableFuture.completedFuture(handle));
        when(handle.pid()).thenReturn(123L);

        try (MockedStatic<ModuleLoader> loaderMock = mockStatic(ModuleLoader.class)) {
            loaderMock.when(ModuleLoader::load).thenReturn(List.of());

            PacosProcess process = PacosProcess.attach(artifact, List.of(), handle);

            assertTrue(process.isAttached());
            assertEquals(0, process.run());
        }
    }

    @Test
    void whenAttachedProcessExitCannotBeObservedWhileAliveThenRunReturnsMinusOne() {
        AppArtifact artifact = new AppArtifact("org.pacos", "engine", "1.0");
        ProcessHandle handle = mock(ProcessHandle.class);
        when(handle.onExit()).thenReturn(CompletableFuture.failedFuture(new IllegalStateException()));
        when(handle.pid()).thenReturn(123L);
        when(handle.isAlive()).thenReturn(true);

        try (MockedStatic<ModuleLoader> loader = mockStatic(ModuleLoader.class)) {
            loader.when(ModuleLoader::load).thenReturn(List.of());
            assertEquals(-1, PacosProcess.attach(artifact, List.of(), handle).run());
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
    void whenChildProcessStopsGracefullyThenStopAndAwaitReturnsTrue() throws Exception {
        AppArtifact artifact = new AppArtifact("org.pacos", "engine", "1.0");
        Process child = mock(Process.class);
        when(child.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(child.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(child.waitFor()).thenReturn(0);
        when(child.isAlive()).thenReturn(true, false);
        when(child.waitFor(10, TimeUnit.SECONDS)).thenReturn(true);
        when(child.exitValue()).thenReturn(0);

        try (MockedStatic<ModuleLoader> loader = mockStatic(ModuleLoader.class);
             MockedStatic<PacosProcess> processMock = mockStatic(PacosProcess.class, CALLS_REAL_METHODS)) {
            loader.when(ModuleLoader::load).thenReturn(List.of());
            processMock.when(() -> PacosProcess.startProcess(org.mockito.ArgumentMatchers.any()))
                    .thenReturn(child);
            PacosProcess process = new PacosProcess(artifact, List.of());
            process.run();

            assertTrue(process.stopAndAwait());
        }
    }

    @Test
    void whenStoppingChildProcessIsInterruptedThenForceStopAndReturnFalse() throws Exception {
        AppArtifact artifact = new AppArtifact("org.pacos", "engine", "1.0");
        Process child = mock(Process.class);
        when(child.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(child.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(child.waitFor()).thenReturn(0);
        when(child.isAlive()).thenReturn(true);
        when(child.waitFor(10, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        try (MockedStatic<ModuleLoader> loader = mockStatic(ModuleLoader.class);
             MockedStatic<PacosProcess> processMock = mockStatic(PacosProcess.class, CALLS_REAL_METHODS)) {
            loader.when(ModuleLoader::load).thenReturn(List.of());
            processMock.when(() -> PacosProcess.startProcess(org.mockito.ArgumentMatchers.any()))
                    .thenReturn(child);
            PacosProcess process = new PacosProcess(artifact, List.of());
            process.run();

            try {
                assertFalse(process.stopAndAwait());
                assertTrue(Thread.currentThread().isInterrupted());
                verify(child).destroyForcibly();
            } finally {
                Thread.interrupted();
            }
        }
    }

    @Test
    void whenAttachedProcessDoesNotStopGracefullyThenForceStopAndAwaitExit() throws Exception {
        AppArtifact artifact = new AppArtifact("org.pacos", "engine", "1.0");
        ProcessHandle handle = mock(ProcessHandle.class);
        CompletableFuture<ProcessHandle> exit = mock();
        when(handle.isAlive()).thenReturn(true, false);
        when(handle.onExit()).thenReturn(exit);
        when(handle.pid()).thenReturn(456L);
        when(exit.get(10, TimeUnit.SECONDS))
                .thenThrow(new TimeoutException())
                .thenReturn(handle);

        try (MockedStatic<ModuleLoader> loader = mockStatic(ModuleLoader.class)) {
            loader.when(ModuleLoader::load).thenReturn(List.of());
            PacosProcess process = PacosProcess.attach(artifact, List.of(), handle);

            assertTrue(process.stopAndAwait());
            verify(handle).destroy();
            verify(handle).destroyForcibly();
        }
    }

    @Test
    void whenAttachedProcessCannotBeStoppedThenStopAndAwaitReturnsFalse() throws Exception {
        AppArtifact artifact = new AppArtifact("org.pacos", "engine", "1.0");
        ProcessHandle handle = mock(ProcessHandle.class);
        CompletableFuture<ProcessHandle> exit = mock();
        when(handle.isAlive()).thenReturn(true);
        when(handle.onExit()).thenReturn(exit);
        when(handle.pid()).thenReturn(456L);
        when(exit.get(10, TimeUnit.SECONDS)).thenThrow(new TimeoutException());

        try (MockedStatic<ModuleLoader> loader = mockStatic(ModuleLoader.class)) {
            loader.when(ModuleLoader::load).thenReturn(List.of());
            PacosProcess process = PacosProcess.attach(artifact, List.of(), handle);

            assertFalse(process.stopAndAwait());
            verify(handle).destroyForcibly();
        }
    }

    @Test
    void whenWaitingForAttachedProcessIsInterruptedThenForceStopAndRestoreInterrupt() throws Exception {
        AppArtifact artifact = new AppArtifact("org.pacos", "engine", "1.0");
        ProcessHandle handle = mock(ProcessHandle.class);
        CompletableFuture<ProcessHandle> exit = mock();
        when(handle.isAlive()).thenReturn(true);
        when(handle.onExit()).thenReturn(exit);
        when(handle.pid()).thenReturn(456L);
        when(exit.get(10, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        try (MockedStatic<ModuleLoader> loader = mockStatic(ModuleLoader.class)) {
            loader.when(ModuleLoader::load).thenReturn(List.of());
            PacosProcess process = PacosProcess.attach(artifact, List.of(), handle);

            try {
                assertFalse(process.stopAndAwait());
                assertTrue(Thread.currentThread().isInterrupted());
                verify(handle).destroyForcibly();
            } finally {
                Thread.interrupted();
            }
        }
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

    private static ProcessHandle processWithArguments(String... arguments) {
        ProcessHandle handle = mock(ProcessHandle.class);
        ProcessHandle.Info info = mock(ProcessHandle.Info.class);
        when(handle.info()).thenReturn(info);
        when(info.arguments()).thenReturn(Optional.of(arguments));
        return handle;
    }
}
