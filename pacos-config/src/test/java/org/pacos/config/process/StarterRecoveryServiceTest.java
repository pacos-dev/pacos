package org.pacos.config.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;

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
    void whenRecoveryArgumentsAreNotConfiguredThenReturnNoArguments() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);
        System.clearProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);

        try {
            assertTrue(StarterRecoveryService.readArguments().isEmpty());

            System.setProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, "  ");
            assertTrue(StarterRecoveryService.readArguments().isEmpty());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, originalValue);
        }
    }

    @Test
    void whenRecoveryArgumentsDecodeToEmptyStringThenReturnNoArguments() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[0]);
        System.setProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, encoded);

        try {
            assertTrue(StarterRecoveryService.readArguments().isEmpty());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, originalValue);
        }
    }

    @Test
    void whenRecoveryArgumentsContainEmptyEntriesThenPreserveThem() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "first\u0000\u0000last".getBytes(StandardCharsets.UTF_8));
        System.setProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, encoded);

        try {
            assertEquals(List.of("first", "", "last"), StarterRecoveryService.readArguments());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, originalValue);
        }
    }

    @Test
    void whenStarterJarPathIsNotConfiguredThenDoNotStartRecovery() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_JAR_PROPERTY);
        System.clearProperty(StarterRecoveryService.STARTER_JAR_PROPERTY);

        try {
            assertFalse(StarterRecoveryService.startInAttachMode());

            System.setProperty(StarterRecoveryService.STARTER_JAR_PROPERTY, "  ");
            assertFalse(StarterRecoveryService.startInRecoveryMode());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_JAR_PROPERTY, originalValue);
        }
    }

    @Test
    void whenStarterJarPathIsInvalidOrMissingThenDoNotStartRecovery() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_JAR_PROPERTY);

        try {
            System.setProperty(StarterRecoveryService.STARTER_JAR_PROPERTY, "\u0000");
            assertFalse(StarterRecoveryService.startInRecoveryMode());

            System.setProperty(StarterRecoveryService.STARTER_JAR_PROPERTY,
                    Path.of(System.getProperty("java.io.tmpdir"), "missing-starter.jar").toString());
            assertFalse(StarterRecoveryService.startInAttachMode());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_JAR_PROPERTY, originalValue);
        }
    }

    @Test
    void whenStarterJarExistsThenStartRecoveryProcess(@TempDir Path tempDirectory) throws Exception {
        String originalJar = System.getProperty(StarterRecoveryService.STARTER_JAR_PROPERTY);
        String originalWorkingDir = System.getProperty("workingDir");
        String originalPid = System.getProperty(StarterRecoveryService.STARTER_PID_PROPERTY);
        Path workingDirectory = tempDirectory.resolve("work");
        Path starterJar = workingDirectory.resolve("starter.jar");
        Files.createDirectories(workingDirectory);
        Files.writeString(starterJar, "not a real JAR; the test only verifies process launch");
        System.setProperty(StarterRecoveryService.STARTER_JAR_PROPERTY, starterJar.toString());
        System.setProperty("workingDir", workingDirectory.toString());
        System.clearProperty(StarterRecoveryService.STARTER_PID_PROPERTY);

        try {
            assertTrue(StarterRecoveryService.startInAttachMode());
            assertTrue(Files.isRegularFile(workingDirectory.resolve("logs/starter-recovery.log")));
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_JAR_PROPERTY, originalJar);
            restoreProperty("workingDir", originalWorkingDir);
            restoreProperty(StarterRecoveryService.STARTER_PID_PROPERTY, originalPid);
        }
    }

    @Test
    void whenRecoveryLogDirectoryIsAFileThenDoNotStartRecovery(@TempDir Path tempDirectory) throws Exception {
        String originalJar = System.getProperty(StarterRecoveryService.STARTER_JAR_PROPERTY);
        String originalWorkingDir = System.getProperty("workingDir");
        Path workingDirectory = tempDirectory.resolve("work");
        Path starterJar = workingDirectory.resolve("starter.jar");
        Files.createDirectories(workingDirectory);
        Files.writeString(starterJar, "not a real JAR");
        Files.writeString(workingDirectory.resolve("logs"), "not a directory");
        System.setProperty(StarterRecoveryService.STARTER_JAR_PROPERTY, starterJar.toString());
        System.setProperty("workingDir", workingDirectory.toString());

        try {
            assertFalse(StarterRecoveryService.startInRecoveryMode());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_JAR_PROPERTY, originalJar);
            restoreProperty("workingDir", originalWorkingDir);
        }
    }

    @Test
    void whenJavaExecutableIsUnavailableThenDoNotStartRecovery(@TempDir Path tempDirectory) throws Exception {
        String originalJar = System.getProperty(StarterRecoveryService.STARTER_JAR_PROPERTY);
        String originalWorkingDir = System.getProperty("workingDir");
        String originalJavaHome = System.getProperty("java.home");
        Path workingDirectory = tempDirectory.resolve("work");
        Path starterJar = workingDirectory.resolve("starter.jar");
        Files.createDirectories(workingDirectory);
        Files.writeString(starterJar, "not a real JAR");
        System.setProperty(StarterRecoveryService.STARTER_JAR_PROPERTY, starterJar.toString());
        System.setProperty("workingDir", workingDirectory.toString());
        System.setProperty("java.home", workingDirectory.resolve("missing-java-home").toString());

        try {
            assertFalse(StarterRecoveryService.startInAttachMode());
        } finally {
            restoreProperty(StarterRecoveryService.STARTER_JAR_PROPERTY, originalJar);
            restoreProperty("workingDir", originalWorkingDir);
            restoreProperty("java.home", originalJavaHome);
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
    void whenParentProcessUsesRelativePathsThenMatchNormalizedInstallationPaths() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle handle = processWithArguments(42L,
                "-DworkingDir=./",
                "-jar",
                "./pacos-starter.jar");

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
    void whenCandidateProcessIsNotAliveThenDoNotTerminateIt() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle candidate = processWithArguments(42L,
                "-DworkingDir=" + workingDirectory,
                "-jar",
                starterJar.toString());
        when(candidate.isAlive()).thenReturn(false);

        StarterRecoveryService.terminateUnresponsiveStarterProcesses(
                starterJar, workingDirectory, 99L, Stream.of(candidate), candidate, 42L);

        verify(candidate, never()).destroyForcibly();
    }

    @Test
    void whenStaleStarterDoesNotExitAfterTerminationThenContinueRecovery() throws Exception {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle staleStarter = processWithArguments(42L,
                "-DworkingDir=" + workingDirectory,
                "-jar",
                starterJar.toString());
        CompletableFuture<ProcessHandle> exit = mock();
        when(staleStarter.isAlive()).thenReturn(true);
        when(staleStarter.onExit()).thenReturn(exit);
        when(exit.get(5, TimeUnit.SECONDS)).thenThrow(new TimeoutException());

        StarterRecoveryService.terminateUnresponsiveStarterProcesses(
                starterJar, workingDirectory, 99L, Stream.of(staleStarter), null, -1L);

        verify(staleStarter).destroyForcibly();
    }

    @Test
    void whenWaitingForStaleStarterIsInterruptedThenRestoreInterruptStatus() throws Exception {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle staleStarter = processWithArguments(42L,
                "-DworkingDir=" + workingDirectory,
                "-jar",
                starterJar.toString());
        CompletableFuture<ProcessHandle> exit = mock();
        when(staleStarter.isAlive()).thenReturn(true);
        when(staleStarter.onExit()).thenReturn(exit);
        when(exit.get(5, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        try {
            StarterRecoveryService.terminateUnresponsiveStarterProcesses(
                    starterJar, workingDirectory, 99L, Stream.of(staleStarter), null, -1L);

            assertTrue(Thread.currentThread().isInterrupted());
            verify(staleStarter).destroyForcibly();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void whenWaitingForStaleStarterFailsAndItRemainsAliveThenContinueRecovery() throws Exception {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle staleStarter = processWithArguments(42L,
                "-DworkingDir=" + workingDirectory,
                "-jar",
                starterJar.toString());
        CompletableFuture<ProcessHandle> exit = mock();
        when(staleStarter.isAlive()).thenReturn(true);
        when(staleStarter.onExit()).thenReturn(exit);
        when(exit.get(5, TimeUnit.SECONDS)).thenThrow(new ExecutionException(new IllegalStateException()));

        StarterRecoveryService.terminateUnresponsiveStarterProcesses(
                starterJar, workingDirectory, 99L, Stream.of(staleStarter), null, -1L);

        verify(staleStarter).destroyForcibly();
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
    void whenProcessArgumentsContainInvalidPathThenDoNotMatchIt() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle handle = processWithArguments(42L,
                "-DworkingDir=\u0000",
                "-jar",
                starterJar.toString());

        assertFalse(StarterRecoveryService.matchesStarterProcess(handle, workingDirectory, starterJar));
    }

    @Test
    void whenProcessArgumentsCannotBeInspectedThenDoNotMatchIt() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        ProcessHandle handle = mock(ProcessHandle.class);
        when(handle.info()).thenThrow(new SecurityException("Process information is restricted"));

        assertFalse(StarterRecoveryService.matchesStarterProcess(
                handle, workingDirectory, workingDirectory.resolve("pacos-starter.jar")));
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
