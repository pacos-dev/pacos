package org.pacos.base.file.archive.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.pacos.base.file.archive.ArchiveNameGenerator;
import org.pacos.base.file.archive.ArchiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ZipArchive {

    private static final Logger LOG = LoggerFactory.getLogger(ZipArchive.class);

    private ZipArchive() {
    }

    public static Path archiveInParentDirectory(Path sourcePath) throws IOException {
        Path zipFilePath =
                ArchiveNameGenerator.generateArchiveName(sourcePath, sourcePath.getParent(), ArchiveType.ZIP);

        LOG.debug("Start compressing {} to {}", sourcePath, zipFilePath);

        FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());

        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipFile(sourcePath.toFile(), sourcePath.getFileName().toString(), zipOut);
        zipOut.close();
        fos.close();

        return zipFilePath;
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }
}