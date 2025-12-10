package org.pacos.base.file.archive.gzip;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import org.pacos.base.file.archive.ArchiveException;
import org.pacos.base.file.archive.ArchiveType;
import org.pacos.base.file.archive.UncompressedNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UnGzipArchive {

    private static final Logger LOG = LoggerFactory.getLogger(UnGzipArchive.class);

    private UnGzipArchive() {
    }

    public static Path uncompress(Path archivePath, Path destinationDir) throws IOException {
        LOG.debug("Start uncompressing {} to {}", archivePath, destinationDir);

        validate(archivePath, destinationDir);
        Path outputPath =
                UncompressedNameGenerator.generateFileName(archivePath, destinationDir, ArchiveType.GZIP);

        try (FileInputStream fis = new FileInputStream(archivePath.toFile());
             GZIPInputStream gis = new GZIPInputStream(new BufferedInputStream(fis));
             FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }

        return outputPath;
    }

    private static void validate(Path compressed, Path destinationDir) throws IOException {
        if (!destinationDir.toFile().isDirectory()) {
            throw new ArchiveException("Destination directory not exists");
        }
        if (!compressed.toFile().isFile()) {
            throw new ArchiveException("Is not a file");
        }
        if (!isGz(compressed)) {
            throw new ArchiveException("Is not a tar.gz file");
        }
    }

    private static boolean isGz(Path filePath) throws IOException {
        String extension = ArchiveType.GZIP.getExtension();
        if (filePath.toString().toLowerCase().endsWith(extension)) {
            try (InputStream is = new FileInputStream(filePath.toString());
                 GZIPInputStream gzis = new GZIPInputStream(is)) {
                return gzis.available() > 0;
            }
        }
        return false;
    }
}
