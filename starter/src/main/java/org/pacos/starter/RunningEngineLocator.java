package org.pacos.starter;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

final class RunningEngineLocator {
    private RunningEngineLocator() {
    }

    static Optional<ProcessHandle> find(Path workingDirectory, Path engineJar, Stream<ProcessHandle> candidates) {
        Path expectedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
        Path expectedEngineJar = engineJar.toAbsolutePath().normalize();
        return candidates.filter(ProcessHandle::isAlive)
                .filter(handle -> isMatchingEngine(handle, expectedWorkingDirectory, expectedEngineJar))
                .findFirst();
    }

    private static boolean isMatchingEngine(
            ProcessHandle handle, Path expectedWorkingDirectory, Path expectedEngineJar) {
        Optional<String[]> optionalArguments = handle.info().arguments();
        if (optionalArguments.isEmpty()) {
            return false;
        }

        List<String> arguments = Arrays.asList(optionalArguments.get());
        boolean matchingWorkingDirectory = arguments.stream()
                .filter(argument -> argument.startsWith("-DworkingDir="))
                .map(argument -> argument.substring("-DworkingDir=".length()))
                .map(path -> resolveProcessPath(path, expectedWorkingDirectory))
                .anyMatch(expectedWorkingDirectory::equals);
        if (!matchingWorkingDirectory) {
            return false;
        }

        for (int index = 0; index + 1 < arguments.size(); index++) {
            if ("-jar".equals(arguments.get(index))) {
                Path candidateJar = resolveProcessPath(arguments.get(index + 1), expectedWorkingDirectory);
                if (expectedEngineJar.equals(candidateJar)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Path resolveProcessPath(String value, Path workingDirectory) {
        Path path = Path.of(value);
        if (!path.isAbsolute()) {
            path = workingDirectory.resolve(path);
        }
        return path.toAbsolutePath().normalize();
    }
}
