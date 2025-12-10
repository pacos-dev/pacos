package org.pacos.base.file.archive.tar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.pacos.base.file.archive.ArchiveNameGenerator;
import org.pacos.base.file.archive.ArchiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TarGzArchive {

    private static final Logger LOG = LoggerFactory.getLogger(TarGzArchive.class);

    private TarGzArchive() {
    }

    public static Path archiveInParentDirectory(Path sourcePath) throws IOException {
        Path tarGzFilePath =
                ArchiveNameGenerator.generateArchiveName(sourcePath, sourcePath.getParent(), ArchiveType.TAR_GZ);

        LOG.debug("Start compressing {} to {}", sourcePath, tarGzFilePath);

        try (FileOutputStream fos = new FileOutputStream(tarGzFilePath.toFile());
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos)) {


            File sourceDir = sourcePath.toFile();
            String sourceDirName = sourceDir.getName();

            packToTar(tos, sourceDir, sourceDirName);

            tos.finish();
        }

        return tarGzFilePath;
    }

    private static void packToTar(TarArchiveOutputStream tos, File fileToPack,
                                  String archiveEntryName) throws IOException {
        TarArchiveEntry tarEntry = new TarArchiveEntry(fileToPack, archiveEntryName);
        tos.putArchiveEntry(tarEntry);
        if (fileToPack.isFile()) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToPack))) {
                byte[] buffer = new byte[1024];
                int count;
                while ((count = bis.read(buffer)) != -1) {
                    tos.write(buffer, 0, count);
                }
            }
            tos.closeArchiveEntry();
        } else if (fileToPack.isDirectory()) {
            tos.closeArchiveEntry();
            File[] children = fileToPack.listFiles();
            if (children != null) {
                for (File child : children) {
                    String childArchiveEntryName = archiveEntryName + "/" + child.getName();
                    packToTar(tos, child, childArchiveEntryName);
                }
            }
        }
    }
}