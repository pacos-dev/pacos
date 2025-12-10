package org.pacos.base.file.archive.gzip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import org.pacos.base.file.archive.ArchiveException;
import org.pacos.base.file.archive.ArchiveNameGenerator;
import org.pacos.base.file.archive.ArchiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gzip allowed only single file to be compressed
 * In case of more files consider tar files first
 */
public final class GzipArchive {

    private static final Logger LOG = LoggerFactory.getLogger(GzipArchive.class);

    private GzipArchive() {
    }

    public static Path archiveSingleFile(Path sourcePath) throws IOException {

        if (sourcePath.toFile().isDirectory()) {
            throw new ArchiveException("Can't gzip directory");
        }
        Path gzipFilePath =
                ArchiveNameGenerator.generateArchiveNameByAppendExtension(sourcePath,
                        sourcePath.getParent(),
                        ArchiveType.GZIP);

        LOG.debug("Start compressing {} to {}", sourcePath, gzipFilePath);
        try (
                FileOutputStream fos = new FileOutputStream(gzipFilePath.toFile());
                GZIPOutputStream gos = new GZIPOutputStream(fos)) {
            compressFile(sourcePath.toFile(), gos);
        }

        return gzipFilePath;
    }

    private static void compressFile(File inputFile, GZIPOutputStream gos) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile))) {

            byte[] buffer = new byte[1024];
            int len;

            while ((len = bis.read(buffer)) > 0) {
                gos.write(buffer, 0, len);
            }
        }
    }

}