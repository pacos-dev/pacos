package org.pacos.config.process;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Isolated("Reads JVM-wide properties used for starter recovery.")
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
            restoreProperty(originalValue);
        }
    }

    @Test
    void whenRecoveryArgumentsAreMalformedThenReturnNoArguments() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);
        System.setProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, "not-valid-base64**");
        try {
            assertTrue(StarterRecoveryService.readArguments().isEmpty());
        } finally {
            restoreProperty(originalValue);
        }
    }

    @Test
    void whenParentProcessIsTheSameStarterForTheSameWorkingDirectoryThenMatchIt() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle handle = processWithArguments(
                "-DworkingDir=" + workingDirectory,
                "-jar",
                starterJar.toString(),
                "--profile=prod");

        assertTrue(StarterRecoveryService.matchesStarterProcess(handle, workingDirectory, starterJar));
    }

    @Test
    void whenRmiIsUnresponsiveThenTerminateOnlyTheMatchingParentStarter() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle parent = processWithArguments(
                "-DworkingDir=" + workingDirectory,
                "-jar",
                starterJar.toString());
        when(parent.isAlive()).thenReturn(true);
        when(parent.onExit()).thenReturn(CompletableFuture.completedFuture(parent));

        StarterRecoveryService.terminateUnresponsiveParentStarter(starterJar, workingDirectory, parent);

        verify(parent).destroyForcibly();
    }

    @Test
    void whenParentDoesNotMatchThisInstallationThenDoNotTerminateIt() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle parent = processWithArguments(
                "-DworkingDir=/opt/other",
                "-jar",
                starterJar.toString());
        when(parent.isAlive()).thenReturn(true);

        StarterRecoveryService.terminateUnresponsiveParentStarter(starterJar, workingDirectory, parent);

        verify(parent, never()).destroyForcibly();
    }

    @Test
    void whenParentProcessUsesAnotherWorkingDirectoryThenDoNotMatchIt() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle handle = processWithArguments(
                "-DworkingDir=/opt/other",
                "-jar",
                starterJar.toString());

        assertFalse(StarterRecoveryService.matchesStarterProcess(handle, workingDirectory, starterJar));
    }

    @Test
    void whenParentProcessUsesAnotherStarterJarThenDoNotMatchIt() {
        Path workingDirectory = Path.of("/opt/pacos").toAbsolutePath().normalize();
        Path starterJar = workingDirectory.resolve("pacos-starter.jar");
        ProcessHandle handle = processWithArguments(
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

    private static ProcessHandle processWithArguments(String... arguments) {
        ProcessHandle handle = mock(ProcessHandle.class);
        ProcessHandle.Info info = mock(ProcessHandle.Info.class);
        when(handle.info()).thenReturn(info);
        when(info.arguments()).thenReturn(Optional.of(arguments));
        return handle;
    }

    private static void restoreProperty(String originalValue) {
        if (originalValue == null) {
            System.clearProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);
        } else {
            System.setProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, originalValue);
        }
    }
}
