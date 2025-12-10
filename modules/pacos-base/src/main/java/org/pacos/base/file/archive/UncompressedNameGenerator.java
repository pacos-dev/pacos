package org.pacos.base.file.archive;

import java.nio.file.Path;

import org.pacos.base.file.AvailableNameGenerator;

public class UncompressedNameGenerator {

    private UncompressedNameGenerator() {
    }

    public static Path generateDirectoryName(Path conpressedPath, Path destination, ArchiveType type) {
        final String archiveName = conpressedPath.getFileName().toString().toLowerCase();
        String destinationDirectory = archiveName.substring(0, archiveName.indexOf(type.getExtension()));
        return generateDestinationLocationPath(destinationDirectory, destination);
    }

    public static Path generateFileName(Path conpressedPath, Path destination, ArchiveType type) {
        final String archiveName = conpressedPath.getFileName().toString().toLowerCase();
        String destinationFileName = archiveName.substring(0, archiveName.indexOf(type.getExtension()));
        return AvailableNameGenerator.generateName(destinationFileName, destination);
    }

    private static Path generateDestinationLocationPath(String destinationDirectoryName, Path directory) {
        final Path destination = directory.resolve(destinationDirectoryName);
        if (destination.toFile().exists()) {
            return generateDestinationLocationPathWithNumber(destinationDirectoryName, directory, 1);
        }
        return destination;
    }

    private static Path generateDestinationLocationPathWithNumber(String destFileName, Path directory, int i) {
        final Path destination = directory.resolve(destFileName + "_" + i);
        if (destination.toFile().exists()) {
            return generateDestinationLocationPathWithNumber(destFileName, directory, i + 1);
        }
        return destination;
    }

}
