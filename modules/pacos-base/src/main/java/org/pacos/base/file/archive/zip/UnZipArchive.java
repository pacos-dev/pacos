package org.pacos.base.file.archive.zip;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.pacos.base.file.archive.ArchiveException;
import org.pacos.base.file.archive.ArchiveType;
import org.pacos.base.file.archive.UncompressedNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UnZipArchive {

    private static final Logger LOG = LoggerFactory.getLogger(UnZipArchive.class);

    private UnZipArchive() {
    }

    public static Path uncompress(Path archivePath, Path destinationDir) throws IOException {
        LOG.debug("Start uncompressing {} to {}", archivePath, destinationDir);

        validate(archivePath, destinationDir);

        Path outputPath = UncompressedNameGenerator.generateDirectoryName(archivePath, destinationDir, ArchiveType.ZIP);
        Path outputRoot = outputPath.toAbsolutePath().normalize();
        Files.createDirectory(outputRoot);

        byte[] buffer = new byte[1024];
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(archivePath.toFile()))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                Path targetPath = outputRoot.resolve(entry.getName()).normalize();
                if (!targetPath.startsWith(outputRoot)) {
                    throw new ArchiveException("Archive entry escapes destination directory: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                        int len;
                        while ((len = zipIn.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }

                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
        return outputPath;
    }

    private static void validate(Path compressed, Path destinationDir) {
        if (!destinationDir.toFile().isDirectory()) {
            throw new ArchiveException("Destination directory not exists");
        }
        if (!compressed.toFile().isFile()) {
            throw new ArchiveException("Is not a file");
        }
        if (!isZipFile(compressed)) {
            throw new ArchiveException("Is not a zip file");
        }
    }

    private static boolean isZipFile(Path path) {
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(path.toFile()))) {
            ZipEntry entry = zipIn.getNextEntry();
            return (entry != null);
        } catch (IOException e) {
            return false;
        }
    }
}