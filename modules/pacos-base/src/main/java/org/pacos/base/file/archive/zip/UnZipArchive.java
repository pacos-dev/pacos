package org.pacos.base.file.archive.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

        byte[] buffer = new byte[1024];
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(archivePath.toFile()))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = outputPath + File.separator + entry.getName();

                if (!entry.isDirectory()) {
                    File parent = new File(new File(filePath).getParent());
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }

                    try (FileOutputStream fos = new FileOutputStream(filePath)) {
                        int len;
                        while ((len = zipIn.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                } else {
                    File dir = new File(filePath);
                    dir.mkdirs();
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
