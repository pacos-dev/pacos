package org.pacos.starter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.config.process.StarterRecoveryService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessCreatorTest {

    @Test
    void whenBuildThenProcessBuilderCreatedWithCorrectCommands(@TempDir Path tempDir) {
        String javaHome = "/fake/java/home";
        List<Path> modules = List.of(tempDir.resolve("module1"), tempDir.resolve("module2"));
        Path jarPath = tempDir.resolve("app.jar");
        List<String> args = List.of("--arg1", "--arg2");

        ProcessBuilder processBuilder = ProcessCreator.build(javaHome, modules, jarPath, args);

        List<String> commands = processBuilder.command();

        assertTrue(commands.contains("/fake/java/home/bin/java"));
        assertTrue(commands.contains("-Dloader.path=" + modules.get(0) + "," + modules.get(1) + ","));
        assertTrue(commands.contains("-jar"));
        assertTrue(commands.contains(jarPath.toString()));
    }

    @Test
    void whenBuildWithStarterMetadataThenForwardJarPathAndArguments(@TempDir Path tempDir) {
        String originalStarterJar = System.getProperty(StarterRecoveryService.STARTER_JAR_PROPERTY);
        List<String> args = List.of("-DworkingDir=" + tempDir, "-Drmi.port=1999");
        System.setProperty(StarterRecoveryService.STARTER_JAR_PROPERTY, tempDir.resolve("starter.jar").toString());

        try {
            ProcessBuilder processBuilder = ProcessCreator.build(
                    "/fake/java/home", List.of(), tempDir.resolve("engine.jar"), args);
            List<String> commands = processBuilder.command();
            String encodedArguments = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    String.join(String.valueOf((char) 0), args).getBytes(StandardCharsets.UTF_8));

            assertTrue(commands.contains("-D" + StarterRecoveryService.STARTER_JAR_PROPERTY + "="
                    + tempDir.resolve("starter.jar")));
            assertTrue(commands.contains("-D" + StarterRecoveryService.STARTER_PID_PROPERTY + "="
                    + ProcessHandle.current().pid()));
            assertTrue(commands.contains("-D" + StarterRecoveryService.STARTER_ARGS_PROPERTY + "=" + encodedArguments));
        } finally {
            if (originalStarterJar == null) {
                System.clearProperty(StarterRecoveryService.STARTER_JAR_PROPERTY);
            } else {
                System.setProperty(StarterRecoveryService.STARTER_JAR_PROPERTY, originalStarterJar);
            }
        }
    }

    @Test
    void whenGetProfileWithValidProfileThenReturnsProfile() {
        List<String> args = List.of("-Dspring.profiles.active=test");

        String profile = ProcessCreator.getProfile(args);

        assertEquals("test", profile);
    }

    @Test
    void whenGetProfileWithNoProfileThenReturnsNull() {
        List<String> args = List.of("--someOtherArg");

        String profile = ProcessCreator.getProfile(args);

        assertNull(profile);
    }

    @Test
    void whenGetProfileWithEmptyProfileThenReturnsNull() {
        List<String> args = List.of("-Dspring.profiles.active=");

        String profile = ProcessCreator.getProfile(args);

        assertNull(profile);
    }
}
