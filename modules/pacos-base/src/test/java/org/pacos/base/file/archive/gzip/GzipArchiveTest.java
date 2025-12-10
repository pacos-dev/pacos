package org.pacos.base.file.archive.gzip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.base.file.archive.ArchiveException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GzipArchiveTest {

    @TempDir
    Path tempDir;

    @Test
    void whenSourcePathIsDirectoryThenThrowsArchiveException() {
        Path directoryPath = tempDir.resolve("testDir");
        directoryPath.toFile().mkdir();

        ArchiveException exception = assertThrows(
                ArchiveException.class,
                () -> GzipArchive.archiveSingleFile(directoryPath)
        );
        assertEquals("Can't gzip directory", exception.getMessage());
    }

    @Test
    void whenSourcePathIsFileThenReturnsGzipFilePath() throws IOException {
        Path sourceFile = Files.createFile(tempDir.resolve("testFile.txt"));
        Files.writeString(sourceFile, "Sample content");

        Path gzipFilePath = GzipArchive.archiveSingleFile(sourceFile);

        assertTrue(Files.exists(gzipFilePath));
        assertTrue(gzipFilePath.toString().endsWith(".gz"));
    }

    @Test
    void whenCompressingFileThenGzipFileIsNotEmpty() throws IOException {
        Path sourceFile = Files.createFile(tempDir.resolve("testFile.txt"));
        Files.writeString(sourceFile, "Sample content");

        Path gzipFilePath = GzipArchive.archiveSingleFile(sourceFile);

        assertTrue(Files.size(gzipFilePath) > 0);
    }

}
