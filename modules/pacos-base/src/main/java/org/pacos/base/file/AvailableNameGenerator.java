package org.pacos.base.file;

import java.nio.file.Path;

/**
 * Generates unique next available name in given destination location for the given source.
 */
public class AvailableNameGenerator {

    private AvailableNameGenerator() {
    }

    /**
     * Generates next available name for given file in any location
     * Returns absolute path for generated name
     */
    public static Path generateName(Path path, Path destination) {
        String fileName = path.getFileName().toString();
        return generateName(fileName, destination);
    }

    public static Path generateName(String fileName, Path destination) {
        String extension = getExtensionIfExists(fileName);
        String fileNameWithoutExtension = getFileNameWithoutExtension(fileName);
        return generateDestinationLocationPath(fileNameWithoutExtension, extension, destination);
    }

    private static String getFileNameWithoutExtension(String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(0, fileName.indexOf("."));
        }
        return fileName;
    }

    private static String getExtensionIfExists(String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(fileName.indexOf("."));
        }
        return "";
    }

    static Path generateDestinationLocationPath(String destFileName, String extension, Path directory) {
        final Path destination = directory.resolve(destFileName + extension);
        if (destination.toFile().exists()) {
            return generateDestinationLocationPathWithNumber(destFileName, directory, extension, 1);
        }
        return destination;
    }

    private static Path generateDestinationLocationPathWithNumber(String destFileName, Path directory, String extension,
            int i) {
        final Path destination = directory.resolve(destFileName + "_" + i + extension);
        if (destination.toFile().exists()) {
            return generateDestinationLocationPathWithNumber(destFileName, directory, extension, i + 1);
        }
        return destination;
    }

}
