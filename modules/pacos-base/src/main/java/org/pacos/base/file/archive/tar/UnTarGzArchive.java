package org.pacos.base.file.archive.tar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.pacos.base.file.archive.ArchiveException;
import org.pacos.base.file.archive.ArchiveType;
import org.pacos.base.file.archive.UncompressedNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UnTarGzArchive {

    private static final Logger LOG = LoggerFactory.getLogger(UnTarGzArchive.class);

    private UnTarGzArchive() {
    }

    public static Path uncompress(Path archivePath, Path destinationDir) throws IOException {
        LOG.debug("Start uncompressing {} to {}", archivePath, destinationDir);

        validate(archivePath, destinationDir);
        Path outputPath =
                UncompressedNameGenerator.generateDirectoryName(archivePath, destinationDir, ArchiveType.TAR_GZ);

        try (FileInputStream fis = new FileInputStream(archivePath.toFile());
             GZIPInputStream gis = new GZIPInputStream(new BufferedInputStream(fis));
             TarArchiveInputStream taris = new TarArchiveInputStream(new BufferedInputStream(gis))) {

            TarArchiveEntry entry;
            while ((entry = taris.getNextTarEntry()) != null) {
                String name = entry.getName();
                File outputFile = outputPath.resolve(name).toFile();

                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    outputFile.getParentFile().mkdirs();
                    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = taris.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
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
        if (!isTarGz(compressed)) {
            throw new ArchiveException("Is not a tar.gz file");
        }
    }

    private static boolean isTarGz(Path filePath) throws IOException {
        String extension = ArchiveType.TAR_GZ.getExtension();
        if (filePath.toString().toLowerCase().endsWith(extension)) {
            try (InputStream is = new FileInputStream(filePath.toString());
                 GZIPInputStream gzis = new GZIPInputStream(is)) {
                byte[] header = new byte[2];
                if (gzis.read(header) == 2 && header[0] == (byte) 0x1F && header[1] == (byte) 0x8B) {
                    byte[] tarHeader = new byte[257];
                    int bytesRead = gzis.read(tarHeader);
                    return bytesRead >= 257 && tarHeader[156] == 'u' && tarHeader[157] == 's' && tarHeader[158] == 't'
                            && tarHeader[159] == 'a' && tarHeader[160] == 'r';
                }
            }
            return true;
        }
        return false;
    }
}
