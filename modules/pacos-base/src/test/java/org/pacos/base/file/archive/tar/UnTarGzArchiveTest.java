package org.pacos.base.file.archive.tar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.base.file.archive.ArchiveException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnTarGzArchiveTest {

    @TempDir
    private Path tempDir;

    @Test
    void whenSourceFileNotExistsThenThrowException() {
        Path file = tempDir.resolve("test.txt");
        assertThrows(ArchiveException.class, () -> UnTarGzArchive.uncompress(file, tempDir));
    }

    @Test
    void whenDestinationDirNotExistsThenThrowException() {
        Path archive = new File("archive/test.tar.gz").toPath();
        Path destination = tempDir.resolve("test");
        assertThrows(ArchiveException.class, () -> UnTarGzArchive.uncompress(archive, destination));
    }

    @Test
    void whenFileIsNotTarGzThenThrowException() throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "content");
        assertThrows(ArchiveException.class, () -> UnTarGzArchive.uncompress(file, tempDir));
    }

    @Test
    void whenFileHasTarGzExtensionOnlyThenThrowException() throws IOException {
        Path file = tempDir.resolve("test.tar.gz");
        Files.writeString(file, "content");
        assertThrows(ZipException.class, () -> UnTarGzArchive.uncompress(file, tempDir));
    }

    @Test
    void whenTarEntryEscapesDestinationThenThrowException() throws IOException {
        Path archive = tempDir.resolve("malicious.tar.gz");
        Path outside = tempDir.resolve("outside.txt");
        byte[] payload = "malicious".getBytes(StandardCharsets.UTF_8);
        try (GzipCompressorOutputStream gzip = new GzipCompressorOutputStream(Files.newOutputStream(archive));
             TarArchiveOutputStream tar = new TarArchiveOutputStream(gzip)) {
            TarArchiveEntry entry = new TarArchiveEntry("../outside.txt");
            entry.setSize(payload.length);
            tar.putArchiveEntry(entry);
            tar.write(payload);
            tar.closeArchiveEntry();
            tar.finish();
        }

        assertThrows(ArchiveException.class, () -> UnTarGzArchive.uncompress(archive, tempDir));
        assertFalse(Files.exists(outside));
    }
}