package org.pacos.base.file.archive.zip;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.base.file.archive.ArchiveException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnZipArchiveTest {

    @TempDir
    private Path tempDir;

    @Test
    void whenSourceFileNotExistsThenThrowException() {
        Path file = tempDir.resolve("test.txt");
        assertThrows(ArchiveException.class, () -> UnZipArchive.uncompress(file, tempDir));
    }

    @Test
    void whenDestinationDirNotExistsThenThrowException() {
        Path archive = new File("archive/test.zip").toPath();
        Path destination = tempDir.resolve("test");
        assertThrows(ArchiveException.class, () -> UnZipArchive.uncompress(archive, destination));
    }

    @Test
    void whenFileIsNotZipThenThrowException() throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "content");
        assertThrows(ArchiveException.class, () -> UnZipArchive.uncompress(file, tempDir));
    }

    @Test
    void whenFileHasZipExtensionOnlyThenThrowException() throws IOException {
        Path file = tempDir.resolve("test.zip");
        Files.writeString(file, "content");
        assertThrows(ArchiveException.class, () -> UnZipArchive.uncompress(file, tempDir));
    }

    @Test
    void whenZipEntryEscapesDestinationThenThrowException() throws IOException {
        Path archive = tempDir.resolve("malicious.zip");
        Path outside = tempDir.resolve("outside.txt");
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(archive))) {
            zipOut.putNextEntry(new ZipEntry("../outside.txt"));
            zipOut.write("malicious".getBytes(StandardCharsets.UTF_8));
            zipOut.closeEntry();
        }

        assertThrows(ArchiveException.class, () -> UnZipArchive.uncompress(archive, tempDir));
        assertFalse(Files.exists(outside));
    }
}
