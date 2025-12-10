package org.pacos.starter;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProcessCreatorTest {

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
