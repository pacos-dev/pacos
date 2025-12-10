package org.pacos.base.file.archive;

import java.nio.file.Path;

/**
 * Generates unique next available name in given destination location for the given source.
 */
public class ArchiveNameGenerator {

    private ArchiveNameGenerator() {
    }

    /**
     * Generates next available name for archive file with given extension
     * Returns absolute path for available name
     */
    public static Path generateArchiveName(Path path, Path destination, ArchiveType type) {
        String destinationName = generateArchiveBaseName(path);
        return generateDestinationLocationPath(destinationName, destination, type);
    }

    public static Path generateArchiveNameByAppendExtension(Path path, Path destination, ArchiveType type) {
        return generateDestinationLocationPath(path.toFile().getName(), destination, type);
    }

    static Path generateDestinationLocationPath(String destFileName, Path directory, ArchiveType type) {
        final Path destination = directory.resolve(destFileName + type.getExtension());
        if (destination.toFile().exists()) {
            return generateDestinationLocationPathWithNumber(destFileName, directory, type, 1);
        }
        return destination;
    }

    private static Path generateDestinationLocationPathWithNumber(String destFileName, Path directory, ArchiveType type,
            int i) {
        final Path destination = directory.resolve(destFileName + "_" + i + type.getExtension());
        if (destination.toFile().exists()) {
            return generateDestinationLocationPathWithNumber(destFileName, directory, type, i + 1);
        }
        return destination;
    }

    static String generateArchiveBaseName(Path path) {
        String name = path.getFileName().toString();
        if (name.contains(".")) {
            name = name.substring(0, name.lastIndexOf("."));
        }
        return name;
    }
}
