package org.pacos.config.process;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Isolated("Reads JVM-wide system properties used for starter recovery.")
class StarterRecoveryServiceTest {

    @Test
    void whenRecoveryArgumentsWerePropagatedThenRestoreOriginalArguments() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);
        List<String> expected = List.of("-DworkingDir=/opt/pacos", "-Drmi.port=1999");
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(
                String.join(String.valueOf((char) 0), expected).getBytes(StandardCharsets.UTF_8));
        System.setProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, encoded);

        try {
            assertEquals(expected, StarterRecoveryService.readArguments());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, originalValue);
        }
    }

    @Test
    void whenRecoveryArgumentsAreMalformedThenReturnNoArguments() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);
        System.setProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, "not-valid-base64**");
        try {
            assertTrue(StarterRecoveryService.readArguments().isEmpty());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, originalValue);
        }
    }

    @Test
    void whenParentProcessIsTheSameStarterForTheSameWorkingDirectoryThenMatchIt() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle handle = processWithArguments(42L,
                "-DworkingDir=" + workingDirectory,
                "-jar",
                starterJar.toString(),
                "--profile=prod");

        assertTrue(StarterRecoveryService.matchesStarterProcess(handle, workingDirectory, starterJar));
    }

    @Test
    void whenRmiIsUnresponsiveThenTerminateMatchingStarterForThisInstallation() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle staleStarter = processWithArguments(42L,
                "-DworkingDir=" + workingDirectory,
                "-jar",
                starterJar.toString());
        when(staleStarter.isAlive()).thenReturn(true);
        when(staleStarter.onExit()).thenReturn(CompletableFuture.completedFuture(staleStarter));

        StarterRecoveryService.terminateUnresponsiveStarterProcesses(
                starterJar, workingDirectory, 99L, Stream.of(staleStarter), staleStarter, 42L);

        verify(staleStarter).destroyForcibly();
    }

    @Test
    void whenParentArgumentsAreUnavailableThenUseTheRecordedStarterPid() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle parent = mock(ProcessHandle.class);
        ProcessHandle.Info info = mock(ProcessHandle.Info.class);
        when(parent.pid()).thenReturn(42L);
        when(parent.isAlive()).thenReturn(true);
        when(parent.info()).thenReturn(info);
        when(info.arguments()).thenReturn(Optional.empty());
        when(parent.onExit()).thenReturn(CompletableFuture.completedFuture(parent));

        StarterRecoveryService.terminateUnresponsiveStarterProcesses(
                starterJar, workingDirectory, 99L, Stream.of(parent), parent, 42L);

        verify(parent).destroyForcibly();
    }

    @Test
    void whenParentDoesNotMatchThisInstallationThenDoNotTerminateIt() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle parent = processWithArguments(42L,
                "-DworkingDir=/opt/other",
                "-jar",
                starterJar.toString());
        when(parent.isAlive()).thenReturn(true);

        StarterRecoveryService.terminateUnresponsiveStarterProcesses(
                starterJar, workingDirectory, 99L, Stream.of(parent), parent, -1L);

        verify(parent, never()).destroyForcibly();
    }

    @Test
    void whenRecoveryProcessMatchesStarterJarThenDoNotTerminateTheNewProcess() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle recoveryProcess = processWithArguments(99L,
                "-DworkingDir=" + workingDirectory,
                "-jar",
                starterJar.toString(),
                "--attach");
        when(recoveryProcess.isAlive()).thenReturn(true);

        StarterRecoveryService.terminateUnresponsiveStarterProcesses(
                starterJar, workingDirectory, 99L, Stream.of(recoveryProcess), null, -1L);

        verify(recoveryProcess, never()).destroyForcibly();
    }

    @Test
    void whenParentProcessUsesAnotherWorkingDirectoryThenDoNotMatchIt() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle handle = processWithArguments(42L,
                "-DworkingDir=/opt/other",
                "-jar",
                starterJar.toString());

        assertFalse(StarterRecoveryService.matchesStarterProcess(handle, workingDirectory, starterJar));
    }

    @Test
    void whenParentProcessUsesAnotherStarterJarThenDoNotMatchIt() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle handle = processWithArguments(42L,
                "-DworkingDir=" + workingDirectory,
                "-jar",
                workingDirectory.resolve("other-starter.jar").toString());

        assertFalse(StarterRecoveryService.matchesStarterProcess(handle, workingDirectory, starterJar));
    }

    @Test
    void whenProcessArgumentsAreUnavailableThenDoNotMatchIt() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        ProcessHandle handle = mock(ProcessHandle.class);
        ProcessHandle.Info info = mock(ProcessHandle.Info.class);
        when(handle.info()).thenReturn(info);
        when(info.arguments()).thenReturn(Optional.empty());

        assertFalse(StarterRecoveryService.matchesStarterProcess(
                handle, workingDirectory, workingDirectory.resolve("pacos-starter.jar")));
    }


    @Test
    void whenRecoveryArgumentsAreUnsetThenReturnNoArguments() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);
        System.clearProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);
        try {
            assertTrue(StarterRecoveryService.readArguments().isEmpty());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, originalValue);
        }
    }

    @Test
    void whenRecoveryArgumentsAreEmptyThenReturnNoArguments() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);
        System.setProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, "");
        try {
            assertTrue(StarterRecoveryService.readArguments().isEmpty());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, originalValue);
        }
    }

    @Test
    void whenRecoveryArgumentsDecodeToEmptyStringThenReturnNoArguments() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);
        System.setProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY,
                Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[0]));
        try {
            assertTrue(StarterRecoveryService.readArguments().isEmpty());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, originalValue);
        }
    }

    @Test
    void whenProcessHasNoMatchingStarterJarArgumentThenDoNotMatchIt() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle handle = processWithArguments(42L,
                "-DworkingDir=" + workingDirectory,
                "-cp", "pacos-starter.jar",
                "org.pacos.starter.PacosRunner");

        assertFalse(StarterRecoveryService.matchesStarterProcess(handle, workingDirectory, starterJar));
    }

    @Test
    void whenWorkingDirectoryArgumentIsMalformedThenDoNotMatchProcess() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        ProcessHandle handle = processWithArguments(42L,
                "-DworkingDir=\\u0000",
                "-jar",
                workingDirectory.resolve("pacos-starter.jar").toString());

        assertFalse(StarterRecoveryService.matchesStarterProcess(
                handle, workingDirectory, workingDirectory.resolve("pacos-starter.jar")));
    }

    @Test
    void whenRecordedStarterPidIsInvalidThenUseFallbackPid() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_PID_PROPERTY);
        System.setProperty(StarterRecoveryService.STARTER_PID_PROPERTY, "invalid");
        try {
            assertEquals(-1, invokeReadRecordedStarterPid());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_PID_PROPERTY, originalValue);
        }
    }

    @Test
    void whenRecordedStarterPidIsBlankThenReturnNegativeOne() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_PID_PROPERTY);
        System.setProperty(StarterRecoveryService.STARTER_PID_PROPERTY, "  ");
        try {
            assertEquals(-1, invokeReadRecordedStarterPid());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_PID_PROPERTY, originalValue);
        }
    }

    private static long invokeReadRecordedStarterPid() {
        try {
            var method = StarterRecoveryService.class.getDeclaredMethod("readRecordedStarterPid");
            method.setAccessible(true);
            return (long) method.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static ProcessHandle processWithArguments(long pid, String... arguments) {
        ProcessHandle handle = mock(ProcessHandle.class);
        ProcessHandle.Info info = mock(ProcessHandle.Info.class);
        when(handle.pid()).thenReturn(pid);
        when(handle.info()).thenReturn(info);
        when(info.arguments()).thenReturn(Optional.of(arguments));
        return handle;
    }

    private static void restoreProperty(String name, String originalValue) {
        if (originalValue == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, originalValue);
        }
    }
}
